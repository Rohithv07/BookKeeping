FROM nginx:alpine

# Remove default Nginx HTML landing page
RUN rm -rf /usr/share/nginx/html/*

# Copy our template so the Nginx entrypoint automatically injects $PORT at runtime
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copy our Vanilla JS static assets
COPY index.html /usr/share/nginx/html/
COPY style.css /usr/share/nginx/html/
COPY app.js /usr/share/nginx/html/
