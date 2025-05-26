FE_LICENSE_REPORT_FILE := .tmp/fe-license-report.html
BE_LICENSE_REPORT_FILE := .tmp/be-license-report.html

help:
	@echo "Usage:"
	@echo "make [task]"
	@echo "  tasks:"
	@echo "    - license-report: generate dependencies license report to license-report.html"
	@echo "    - build-jar: execute gradlew clean and than build, result in dgrv4_Gateway_serv/build/libs/digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/open-source-version.txt).jar"
	@echo "    - java-run: run digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/open-source-version.txt).jar using java -jar command"
	@echo "    - build-image: build docker image"
	@echo "    - run-container: run docker container"
license-report:
	@echo "========== Generate BE License Report =========="
	@rm -f $(BE_LICENSE_REPORT_FILE) || true
	@rm -f $(FE_LICENSE_REPORT_FILE) || true
	@rm -rf build/licenses || true
	@echo '{"allowedLicenses": []}' | jq > .tmp/allowed-licenses.json
	@sh gradlew -p . checkLicense || true
	@cat build/licenses/dependencies-without-allowed-license.json | jq '.dependenciesWithoutAllowedLicenses[].moduleLicense' | sort | uniq | jq --slurp '.' | jq '{allowedLicenses:[{moduleLicense:.[],moduleName:".*"}]}' > .tmp/allowed-licenses.json
	@sh gradlew -p . checkLicense
	@rm .tmp/allowed-licenses.json
	@mv build/licenses/index.html $(BE_LICENSE_REPORT_FILE)
	@rm -rf build/licenses
	@echo "---------- BE License Report Generated ----------"
	@echo "BE License Report file: $(shell realpath $(BE_LICENSE_REPORT_FILE))"
	@echo "========== Generate FE License Report =========="
	@cp dgrv4_Gateway_serv/srcAngular/package.json .
	@npm install --force
	@license-report --output=html > $(FE_LICENSE_REPORT_FILE)
	@echo "---------- FE License Report Generated ----------"
	@echo "FE License Report file: $(shell realpath $(FE_LICENSE_REPORT_FILE))"
	@rm -rf node_modules
	@rm -f package.json
	@rm -f package-lock.json

build-jar:
	@sh gradlew :dgrv4_Gateway_serv:clean
	@sh gradlew :dgrv4_Gateway_serv:build
java-run:
	java -jar dgrv4_Gateway_serv/build/libs/digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/open-source-version.txt).jar --digiRunner.token.key-store.path=$(shell pwd)/dgrv4_Gateway_serv/keys
build-image:
	@docker build -t digirunner .
run-container:
	@docker run -p 18080:18080 digirunner