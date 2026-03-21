# ──────────────────────────────────────────────────────────────────────────────
# Dockerfile — Selenium Java BDD Framework
# Base: Eclipse Temurin 17 (LTS) on Ubuntu Jammy
# Includes: Java 17, Maven 3.9, Chrome, ChromeDriver, Allure CLI
# ──────────────────────────────────────────────────────────────────────────────

FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="Nishant Jaiswal <nishant7jaiswal@gmail.com>"
LABEL description="Selenium Java BDD Test Automation Framework"
LABEL version="1.0.0"

# ── Build Arguments ───────────────────────────────────────────────────────────
ARG MAVEN_VERSION=3.9.6
ARG ALLURE_VERSION=2.25.0
ARG CHROME_VERSION=stable

# ── Environment Variables ─────────────────────────────────────────────────────
ENV MAVEN_HOME=/opt/maven
ENV ALLURE_HOME=/opt/allure
ENV PATH="${MAVEN_HOME}/bin:${ALLURE_HOME}/bin:${PATH}"
ENV JAVA_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"

# ── System Dependencies ───────────────────────────────────────────────────────
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    unzip \
    gnupg \
    ca-certificates \
    fonts-liberation \
    libappindicator3-1 \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libcups2 \
    libdbus-1-3 \
    libdrm2 \
    libgbm1 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libx11-xcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxkbcommon0 \
    libxrandr2 \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# ── Google Chrome ─────────────────────────────────────────────────────────────
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | \
    gpg --dearmor -o /usr/share/keyrings/google-linux-signing-key.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux-signing-key.gpg] \
    http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends google-chrome-${CHROME_VERSION} && \
    rm -rf /var/lib/apt/lists/*

# ── Maven ─────────────────────────────────────────────────────────────────────
RUN wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    -O /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    mv /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    rm /tmp/maven.tar.gz

# ── Allure CLI ────────────────────────────────────────────────────────────────
RUN wget -q "https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.tgz" \
    -O /tmp/allure.tgz && \
    tar -xzf /tmp/allure.tgz -C /opt && \
    mv /opt/allure-${ALLURE_VERSION} ${ALLURE_HOME} && \
    rm /tmp/allure.tgz

# ── Working Directory ─────────────────────────────────────────────────────────
WORKDIR /app

# ── Cache Maven Dependencies ──────────────────────────────────────────────────
# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# ── Copy Source ───────────────────────────────────────────────────────────────
COPY src ./src
COPY testng.xml .
COPY .mvn ./.mvn 2>/dev/null || true

# ── Output Directories ────────────────────────────────────────────────────────
RUN mkdir -p target/allure-results target/screenshots target/logs target/cucumber-reports

# ── Entrypoint Script ─────────────────────────────────────────────────────────
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

# Default: run smoke tests headless
CMD ["test", "-Dtags=@smoke", "-Dheadless=true", "-Dbrowser=chrome"]
