FROM nginx:1.27-alpine

LABEL project="docker-buildlab-selfapp"

COPY selfapp.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1
