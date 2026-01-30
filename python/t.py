import jwt
import base64

token ="eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidHlwZSI6ImFjY2Vzc190b2tlbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzY5NzU4MjEzLCJleHAiOjE3NzAzNjMwMTN9.CTZrY6vfLTzQLLKaiXWDgElnc8UD9eF0kOjeBiSW3CY"

secret_base64 = "NBZzu/XN0IgTPw/EfJgOkYD+tK5JdLLhQdNkUsPl2AU="
secret_bytes = base64.b64decode(secret_base64)

try:
    decoded = jwt.decode(
        token,
        secret_bytes,
        algorithms=["HS256"]
    )

    print("✅ Token hợp lệ")
    print(decoded)

except jwt.ExpiredSignatureError:
    print("❌ Token hết hạn")

except jwt.InvalidTokenError as e:
    print("❌ Token không hợp lệ:", e)
