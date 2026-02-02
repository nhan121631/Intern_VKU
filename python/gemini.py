from flask import Flask, request, jsonify
from flask_cors import CORS
import os, datetime
import mysql.connector
import requests, json
from dotenv import load_dotenv
import time
import jwt
import base64
from functools import wraps
from sshtunnel import SSHTunnelForwarder

load_dotenv()

app = Flask(__name__)
CORS(app)

# =========================
# SSH TUNNEL & DATABASE CONFIG
# =========================

USE_SSH_TUNNEL = os.getenv("USE_SSH_TUNNEL", "false").lower() == "true"
SSH_HOST = os.getenv("SSH_HOST", "18.143.194.168")
SSH_USER = os.getenv("SSH_USER", "ec2-user")
SSH_KEY_PATH = os.getenv("PRIVATE_KEY_PATH", "./my-backend-key.pem")

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DATABASE = os.getenv("DB_NAME", "job_management")
USER = os.getenv("MYSQL_USER", "root")
PASSWORD = os.getenv("MYSQL_PASSWORD", "root_password")

# Global SSH tunnel
ssh_tunnel = None

def init_ssh_tunnel():
    """Initialize SSH tunnel on app startup"""
    global ssh_tunnel
    
    if not USE_SSH_TUNNEL:
        return
    
    try:
        print(f"[INFO] Initializing SSH tunnel to {SSH_HOST} using key: {SSH_KEY_PATH}")
        
        # Xử lý path: nếu relative, tính từ thư mục chứa script
        import os.path
        if not os.path.isabs(SSH_KEY_PATH):
            script_dir = os.path.dirname(os.path.abspath(__file__))
            key_path = os.path.join(script_dir, SSH_KEY_PATH)
        else:
            key_path = SSH_KEY_PATH
        
        key_path = os.path.abspath(key_path)
        
        if not os.path.exists(key_path):
            print(f"[ERROR] Private key not found: {key_path}")
            raise FileNotFoundError(f"Private key not found: {key_path}")
        
        ssh_tunnel = SSHTunnelForwarder(
            (SSH_HOST, 22),
            ssh_username=SSH_USER,
            ssh_pkey=key_path,
            allow_agent=False,
            host_pkey_directories=[],
            remote_bind_address=('127.0.0.1', DB_PORT),
            local_bind_address=('127.0.0.1', 0),
            set_keepalive=10
        )
        ssh_tunnel.start()
        print(f"[SUCCESS] SSH tunnel active on local port {ssh_tunnel.local_bind_port}")
        
    except Exception as e:
        print(f"[WARNING] Failed to initialize SSH tunnel: {e}")
        print(f"[INFO] Will attempt direct connection on first request")
        ssh_tunnel = None

def get_db_connection():
    """Create database connection with or without SSH tunnel"""
    global ssh_tunnel
    
    if USE_SSH_TUNNEL:
        try:
            # Kiểm tra và dùng lại tunnel đã tạo sẵn
            if ssh_tunnel is None or not ssh_tunnel.is_active:
                print(f"[WARNING] SSH tunnel not active, reinitializing...")
                init_ssh_tunnel()
            
            if ssh_tunnel is None:
                raise Exception("SSH tunnel initialization failed")
            
            # Kết nối qua tunnel
            print(f"[DEBUG] Connecting to MySQL via tunnel on port {ssh_tunnel.local_bind_port}...")
            
            conn = mysql.connector.connect(
                host='127.0.0.1',
                port=ssh_tunnel.local_bind_port,
                user=USER,
                password=PASSWORD,
                database=DATABASE,
                connect_timeout=5,
                autocommit=True,
                use_pure=True
            )
            print(f"[INFO] MySQL connection via tunnel successful")
            return conn
            
        except Exception as e:
            print(f"[ERROR] SSH tunnel connection failed: {e}")
            print(f"[INFO] Attempting direct connection to {SSH_HOST}:{DB_PORT}...")
            # Fallback: thử kết nối trực tiếp đến EC2
            try:
                conn = mysql.connector.connect(
                    host=SSH_HOST,
                    port=DB_PORT,
                    user=USER,
                    password=PASSWORD,
                    database=DATABASE,
                    connect_timeout=5,
                    autocommit=True
                )
                print(f"[INFO] Direct connection to EC2 successful")
                return conn
            except Exception as e2:
                print(f"[ERROR] Direct connection also failed: {e2}")
                raise Exception(f"Both tunnel and direct connection failed. Tunnel: {e}, Direct: {e2}")
    else:
        # Kết nối trực tiếp local/remote
        return mysql.connector.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=USER,
            password=PASSWORD,
            database=DATABASE
        )

# =========================
# JWT CONFIG (Spring Compatible)
# =========================

HOST = DB_HOST
PORT = DB_PORT
    

def get_jwt_secret_bytes():
    secret_b64 = os.getenv("JWT_SECRET_BASE64")
    return base64.b64decode(secret_b64)

def verify_jwt_token(token):
    try:
        decoded = jwt.decode(
            token,
            get_jwt_secret_bytes(),
            algorithms=["HS256"]
        )

        # giống logic Spring
        if decoded.get("type") != "access_token":
            return None, "Invalid token type"

        return decoded, None

    except jwt.ExpiredSignatureError:
        return None, "Token expired"

    except jwt.InvalidTokenError as e:
        return None, str(e)

def jwt_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):

        auth_header = request.headers.get("Authorization")

        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Missing token"}), 401

        token = auth_header.split(" ")[1]

        decoded, err = verify_jwt_token(token)

        if err:
            return jsonify({"error": err}), 401

        request.user = decoded
        return f(*args, **kwargs)

    return decorated

# =========================
# TASK CACHE (phân biệt theo userId và admin/user)
# =========================

def build_task_objects(result, columns):
    """Convert DB rows to JSON task objects"""
    tasks = []
    for row in result:
        task_obj = {
            "id": int(row[0]) if row[0] is not None else None,
            "createdAt": row[1].isoformat() if isinstance(row[1], datetime.datetime) else str(row[1]),
            "title": str(row[2]) if row[2] else "",
            "description": str(row[3]) if row[3] else "",
            "status": str(row[4]) if row[4] else "",
            "deadline": row[5].isoformat() if isinstance(row[5], datetime.date) else str(row[5]),
            "assignedUserId": int(row[6]) if row[6] is not None else None,
            "allowUserUpdate": bool(row[7]) if row[7] is not None else False,
            "assignedFullName": str(row[8]) if row[8] else ""
        }
        tasks.append(task_obj)
    return tasks

def get_roles(userId):
    
    query = f"SELECT r.name FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = {userId};"
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute(query)
        result = cursor.fetchall()
        cursor.close()
        conn.close()

        roles = [row[0] for row in result]
        print(f"[DEBUG] User {userId} roles: {roles}")
        return roles
    except Exception as e:
        print(f"[ERROR] Database connection failed: {e}")
        return []
    

def get_tasks(userId):
    
    roles = get_roles(userId)
    is_admin = any(role.upper() in ['ADMIN', 'ADMINISTRATORS'] for role in roles)
    
    # Query database
    if is_admin:
        query = '''SELECT t.id, t.createddate as createdAt, t.title, t.description, t.status, t.deadline, t.user_id as assignedUserId, t.allow_user_update as allowUserUpdate , up.full_name from tasks t join users u on t.user_id = u.id 
join user_profiles up on u.profile_id = up.id'''
        print(f"[DEBUG] Admin query - fetching ALL tasks")
    else:
        query = f'''SELECT t.id, t.createddate as createdAt, t.title, t.description, t.status, t.deadline, t.user_id as assignedUserId, t.allow_user_update as allowUserUpdate , up.full_name from tasks t join users u on t.user_id = u.id 
join user_profiles up on u.profile_id = up.id where u.id = {userId}'''
        print(f"[DEBUG] User query - fetching tasks for user_id={userId}")

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute(query)
        result = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
        cursor.close()
        conn.close()

        print(f"[DEBUG] Fetched {len(result)} tasks for {'admin' if is_admin else f'user {userId}'}")

        return result, columns

    except Exception as e:
        print(f"[ERROR] Database connection failed: {e}")
        return [], None

# =========================
# AI CHATBOT API (Protected by JWT)
# =========================

@app.route('/ai_chatbot', methods=['POST'])
@jwt_required
def ai_chatbot():

    # ✅ lấy user từ JWT
    user_id = request.user.get("id")
    username = request.user.get("sub")
    print(f"[CHAT] user_id={user_id} username={username}")

    data = request.get_json()
    history = data.get("history", [])

    result, columns = get_tasks(user_id)


    if not result or columns is None:
        return jsonify({
            "isJsonArray": False,
            "reply": "No available task data."
        })

    # Build structured task data
    task_objects = build_task_objects(result, columns)
    
    # Create table format for AI
    task_table = "ID | Title | Description | Status | Deadline | Assigned User | AllowUpdate | Created\n"
    task_table += "-" * 100 + "\n"
    
    for task in task_objects:
        task_table += f"{task['id']} | {task['title']} | {task['description'][:30]} | {task['status']} | {task['deadline']} | {task['assignedFullName']} | {task['allowUserUpdate']} | {task['createdAt']}\n"
    
    if not history or '# VAI TRÒ' not in str(history[0]):
        initial_prompt = f"""# VAI TRÒ
        Bạn là **TaskManagement Assistant** - chuyên gia quản lý và tư vấn công việc, với khả năng phân tích dữ liệu task và đưa ra giải pháp tối ưu.

        # MỤC TIÊU
        - **Tìm kiếm & Lọc**: Xác định chính xác các công việc theo yêu cầu người dùng (theo người thực hiện, trạng thái, deadline, từ khóa)
        - **Tư vấn**: Đưa ra lời khuyên về quản lý công việc, ưu tiên task, phân bổ thời gian
        - **Phân tích**: Thống kê và phân tích tình trạng công việc

        # BỐI CẢNH
        Người dùng đang làm việc với hệ thống quản lý task. Dữ liệu công việc bao gồm:
        - **ID**: Mã định danh công việc
        - **Title**: Tiêu đề công việc
        - **Description**: Mô tả chi tiết
        - **Status**: Trạng thái (OPEN, IN_PROGRESS, DONE, CANCELED)
        - **Deadline**: Hạn hoàn thành
        - **Assigned User**: Người được gán
        - **AllowUpdate**: Cho phép người dùng cập nhật hay không
        - **CreatedAt**: Ngày tạo

        # CẤU TRÚC TRẢ LỜI

        ## 1. Yêu cầu TÌM/LIỆT KÊ/LỌC công việc cụ thể:
        **Format bắt buộc**: `TASK_IDS:[1,2,3,5]`

        **Ví dụ câu hỏi**:
        - "Tìm công việc của Nguyễn Văn A"
        - "Liệt kê task đang IN_PROGRESS"
        - "Công việc nào hết hạn hôm nay?"
        - "Cho tôi xem task có deadline trong tuần này"

        **Cách trả lời**: Phân tích bảng dữ liệu → Lọc ID phù hợp → Trả về `TASK_IDS:[...]`

        ## 2. Yêu cầu TƯ VẤN/GIẢI THÍCH/PHÂN TÍCH:
        **Format**: Văn bản thông thường, thân thiện, ngắn gọn (2-4 câu)

        **Ví dụ câu hỏi**:
        - "Làm thế nào để quản lý task hiệu quả?"
        - "Task nào tôi nên làm trước?"
        - "Tại sao task này quan trọng?"
        - "Giải thích trạng thái IN_PROGRESS"

        **Cách trả lời**: Đưa ra lời khuyên dựa trên dữ liệu (nếu có) hoặc kinh nghiệm quản lý công việc

        # GIỚI HẠN & QUY TẮC
        1. ❌ **KHÔNG** trả lời câu hỏi không liên quan đến quản lý công việc
        2. ✅ **CHỈ** sử dụng dữ liệu từ bảng bên dưới, không bịa đặt thông tin
        3. ✅ **PHẢI** trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp
        4. ✅ Nếu không tìm thấy kết quả → Thông báo rõ ràng và gợi ý cách tìm khác
        5. ✅ Khi lọc task → Ưu tiên độ chính xác, chỉ trả về ID thực sự khớp yêu cầu

        # DỮ LIỆU CÔNG VIỆC
        ```
        {task_table}
        ```

        ---
        **Hãy phân tích kỹ yêu cầu người dùng và chọn format trả lời phù hợp!**
        """

        history = [{'role': 'user', 'text': initial_prompt}] + history
    else:
        # Inject task data context without full prompt if already initialized
        if task_table not in str(history):
            context_update = f"\n# DỮ LIỆU CÔNG VIỆC CẬP NHẬT\n```\n{task_table}\n```"
            history.insert(1, {'role': 'user', 'text': context_update})

    models_to_try = [
        "gemini-2.5-flash",
        "gemini-flash-latest",
        "gemini-2.0-flash-lite",
        "gemini-2.0-flash",
        "gemini-pro-latest",
    ]

    API_KEY = os.getenv("API_KEY")

    parts = []
    for turn in history:
        if turn['role'] == 'user':
            parts.append({"text": f"Bạn: {turn['text']}"})
        else:
            parts.append({"text": turn['text']})

    payload = {"contents": [{"parts": parts}]}
    headers = {"Content-Type": "application/json"}

    last_error = None

    for MODEL in models_to_try:
        try:
            URL = f"https://generativelanguage.googleapis.com/v1beta/models/{MODEL}:generateContent?key={API_KEY}"

            resp = requests.post(
                URL, headers=headers,
                data=json.dumps(payload),
                timeout=15
            )

            if resp.status_code in [404, 429]:
                last_error = resp.text
                time.sleep(1)
                continue

            resp.raise_for_status()

            result = resp.json()
            reply = result["candidates"][0]["content"]["parts"][0]["text"]

            # Check if AI returned task IDs
            if "TASK_IDS:" in reply:
                try:
                    # Extract task IDs from reply
                    import re
                    match = re.search(r'TASK_IDS:\[(.*?)\]', reply)
                    if match:
                        ids_str = match.group(1)
                        task_ids = [int(x.strip()) for x in ids_str.split(',') if x.strip()]
                        
                        # Filter tasks by IDs
                        filtered_tasks = [t for t in task_objects if t['id'] in task_ids]
                        
                        return jsonify({
                            "isJsonArray": True,
                            "reply": filtered_tasks
                        })
                except Exception as e:
                    print(f"[WARN] Failed to parse TASK_IDS: {e}")
            
            # Return text response
            return jsonify({
                "isJsonArray": False,
                "reply": reply
            })

        except Exception as e:
            last_error = str(e)
            continue

    print("[FATAL]", last_error)

    return jsonify({
        "errors": last_error,
    })

# =========================

@app.teardown_appcontext
def cleanup(exception=None):
    """Close SSH tunnel when app shuts down"""
    global ssh_tunnel
    if ssh_tunnel and ssh_tunnel.is_active:
        print("[INFO] Closing SSH tunnel...")
        ssh_tunnel.stop()

if __name__ == '__main__':
    try:
        # Khởi tạo SSH tunnel trước khi start app
        init_ssh_tunnel()
        
        app.run(host="0.0.0.0", port=5001, debug=True)
    finally:
        if ssh_tunnel and ssh_tunnel.is_active:
            ssh_tunnel.stop()
            print("[INFO] SSH tunnel closed")
