FROM nginx:alpine

# Remove default Nginx HTML landing page
RUN rm -rf /usr/share/nginx/html/*

# Copy our custom Nginx server configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy our Vanilla JS static assets
COPY index.html /usr/share/nginx/html/
COPY style.css /usr/share/nginx/html/
COPY app.js /usr/share/nginx/html/

# Expose port (Google Cloud Run typically listens on 8080)
EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
