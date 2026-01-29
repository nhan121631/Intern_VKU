/**
 * Vercel Serverless Function
 * Proxies Waline comment API requests to avoid CORS and 403 issues
 */

export default async function handler(req, res) {
  try {
    // Get Waline server URL from environment variable
    const WALINE_SERVER_URL = process.env.WALINE_SERVER_URL || process.env.VITE_WALINE_SERVER_URL;
    
    if (!WALINE_SERVER_URL) {
      console.error('WALINE_SERVER_URL not configured');
      return res.status(500).json({ 
        errno: 500, 
        errmsg: 'Server configuration error: WALINE_SERVER_URL not set' 
      });
    }

    // Build the target URL with query params
    const queryString = new URLSearchParams(req.query).toString();
    const targetUrl = `${WALINE_SERVER_URL.replace(/\/$/, '')}/api/comment${queryString ? '?' + queryString : ''}`;

    console.log(`[Waline Proxy] ${req.method} ${targetUrl}`);

    // Prepare headers for upstream request
    const headers = {
      'Content-Type': 'application/json',
      'User-Agent': 'Vercel-Proxy/1.0',
    };

    // Add authorization if available (for admin operations)
    if (process.env.WALINE_API_TOKEN) {
      headers['Authorization'] = `Bearer ${process.env.WALINE_API_TOKEN}`;
    }

    // Forward the request to Waline server
    const upstreamResponse = await fetch(targetUrl, {
      method: req.method,
      headers: headers,
      body: req.method !== 'GET' && req.method !== 'HEAD' 
        ? JSON.stringify(req.body) 
        : undefined,
    });

    // Get response data
    const contentType = upstreamResponse.headers.get('content-type');
    let data;
    
    if (contentType && contentType.includes('application/json')) {
      data = await upstreamResponse.json();
    } else {
      data = await upstreamResponse.text();
    }

    console.log(`[Waline Proxy] Response status: ${upstreamResponse.status}`);

    // Forward the response back to client
    res.status(upstreamResponse.status).json(data);

  } catch (error) {
    console.error('[Waline Proxy] Error:', error);
    res.status(500).json({ 
      errno: 500, 
      errmsg: 'Proxy error: ' + error.message 
    });
  }
}
