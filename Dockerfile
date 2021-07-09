FROM openjdk:11
COPY output/url-shortener-0.1-all.jar app.jar
COPY utils/start_app.sh start_app.sh
RUN chmod +x start_app.sh
EXPOSE 8080
ENTRYPOINT ["./start_app.sh"]