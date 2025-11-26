# Nginx Configuration for Custom Domain Support

This directory contains Nginx configuration files for routing both subdomain requests (tenant1.liyaqa.com) and custom domain requests (gym.example.com) to the appropriate tenants.

## Prerequisites

For custom domain support with automatic SSL certificate provisioning, you need:

1. **OpenResty** - Nginx with Lua support
2. **lua-resty-auto-ssl** - Automatic SSL certificate management with Let's Encrypt

## Installation

### 1. Install OpenResty

#### Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install -y openresty
```

#### CentOS/RHEL:
```bash
sudo yum install -y openresty
```

### 2. Install lua-resty-auto-ssl

```bash
sudo opm install GUI/lua-resty-auto-ssl
```

### 3. Create Required Directories

```bash
sudo mkdir -p /etc/resty-auto-ssl
sudo chown -R www-data:www-data /etc/resty-auto-ssl
```

### 4. Generate Fallback Certificate

```bash
sudo openssl req -new -newkey rsa:2048 -days 3650 -nodes -x509 \
  -subj '/CN=sni-support-required-for-valid-ssl' \
  -keyout /etc/ssl/resty-auto-ssl-fallback.key \
  -out /etc/ssl/resty-auto-ssl-fallback.crt
```

## Configuration

### 1. Main Nginx Configuration

Add this to your main `nginx.conf` (usually at `/usr/local/openresty/nginx/conf/nginx.conf`):

```nginx
# At the top of http block
http {
    # ... other configurations ...

    # lua-resty-auto-ssl configuration
    lua_shared_dict auto_ssl 1m;
    lua_shared_dict auto_ssl_settings 64k;

    # The following is very important for Let's Encrypt validation
    resolver 8.8.8.8 8.8.4.4 ipv6=off;

    init_by_lua_block {
        auto_ssl = (require "resty.auto-ssl").new()

        -- Define a function to determine which SNI domains to automatically handle
        auto_ssl:set("allow_domain", function(domain)
            -- Add your custom logic here to determine which domains
            -- should be allowed to automatically generate SSL certificates
            -- For now, allow all domains
            return true
        end)

        auto_ssl:init()
    }

    init_worker_by_lua_block {
        auto_ssl:init_worker()
    }

    # Include the tenant routing configuration
    include /path/to/tenant-routing.conf;
}
```

### 2. Copy Tenant Routing Configuration

```bash
sudo cp tenant-routing.conf /usr/local/openresty/nginx/conf/tenant-routing.conf
```

### 3. Update Backend Upstream

Edit `tenant-routing.conf` and update the `upstream backend` section with your actual backend servers:

```nginx
upstream backend {
    server your-backend-host:8080;
    # Add more servers for load balancing if needed
}
```

### 4. Update Wildcard SSL Certificate Path

Make sure you have a wildcard SSL certificate for `*.liyaqa.com` and update the paths in the configuration:

```nginx
ssl_certificate /etc/letsencrypt/live/liyaqa.com/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/liyaqa.com/privkey.pem;
```

To obtain a wildcard certificate with Let's Encrypt:

```bash
sudo certbot certonly --manual --preferred-challenges dns \
  -d liyaqa.com -d *.liyaqa.com
```

### 5. Test and Reload Nginx

```bash
# Test configuration
sudo openresty -t

# Reload Nginx
sudo systemctl reload openresty
```

## How It Works

### Subdomain Routing (tenant1.liyaqa.com)

1. User visits `https://tenant1.liyaqa.com`
2. Nginx extracts "tenant1" from the subdomain
3. Request is proxied to backend with `X-Tenant-Slug: tenant1` header
4. Backend's `TenantContextFilter` reads the header and loads the tenant

### Custom Domain Routing (gym.example.com)

1. Tenant configures custom domain `gym.example.com` in the app
2. Tenant adds DNS records:
   - TXT record: `_liyaqa-verification` with verification token
   - CNAME record: `gym.example.com` pointing to `tenant1.liyaqa.com`
3. User visits `https://gym.example.com`
4. lua-resty-auto-ssl automatically provisions SSL certificate via Let's Encrypt
5. Nginx proxies request to backend with `X-Custom-Domain: gym.example.com` header
6. Backend's `TenantContextFilter` looks up tenant by custom domain

## Security Considerations

1. **Domain Validation**: The `allow_domain` function should implement proper validation to prevent abuse
2. **Rate Limiting**: Implement rate limiting to prevent SSL certificate provisioning abuse
3. **Monitoring**: Monitor SSL certificate generation and renewal
4. **Firewall**: Ensure only ports 80 and 443 are open

## Troubleshooting

### SSL Certificate Not Being Generated

1. Check that ports 80 and 443 are accessible from the internet
2. Verify DNS records are properly configured
3. Check Nginx error logs: `sudo tail -f /usr/local/openresty/nginx/logs/error.log`
4. Verify Let's Encrypt rate limits haven't been exceeded

### Custom Domain Not Resolving

1. Verify CNAME record is properly configured
2. Check DNS propagation: `dig gym.example.com`
3. Verify tenant has the custom domain configured in the database
4. Check backend logs for tenant resolution

## Production Recommendations

1. **Use a CDN**: Consider using CloudFlare or AWS CloudFront for additional security and performance
2. **Database Connection Pooling**: Ensure efficient database lookups for custom domain resolution
3. **Caching**: Implement caching for tenant lookups to reduce database load
4. **Monitoring**: Set up monitoring for SSL certificate expiration and renewal
5. **Backup**: Regularly backup SSL certificates and configuration

## Alternative: Using CloudFlare

For simpler SSL management, consider using CloudFlare:

1. Point custom domains to CloudFlare
2. Configure CloudFlare to proxy requests
3. Use CloudFlare's Universal SSL for automatic certificate provisioning
4. Configure CloudFlare to forward requests to your origin server

This approach eliminates the need for lua-resty-auto-ssl and simplifies SSL management.
