FROM nginx:alpine

# Remove default Nginx HTML landing page
RUN rm -rf /usr/share/nginx/html/*

# Copy our custom Nginx config containing the __PORT__ placeholder
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy our Vanilla JS static assets
COPY index.html /usr/share/nginx/html/
COPY style.css /usr/share/nginx/html/
COPY app.js /usr/share/nginx/html/
CMD sed -i -e 's/__PORT__/'"$PORT"'/g' /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'
