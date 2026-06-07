FROM azul/zulu-openjdk:17-jre

RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libglib2.0-0 \
    libx11-6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfontconfig1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/gestionmorgue-*.jar app.jar

ENV JAVA_OPTS="-Xmx512m"

ENV PG_HOST=db
ENV PG_PORT=5432
ENV PG_DB=gestionmorgue
ENV PG_USER=gestionmorgue
ENV PG_PASSWORD=gestionmorgue

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --db=postgresql"]
