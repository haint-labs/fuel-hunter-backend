OWNER = sashjakk
NAME = fuel-hunter
VERSION ?= 0.0.4

SSH_SERVER_NAME = digital-ocean

ARTIFACT_NAME = $(OWNER)/$(NAME):$(VERSION)

all: build push deploy

.PHONY: build
build:
	docker build -t $(ARTIFACT_NAME) .


.PHONY: push
push:
	docker push $(ARTIFACT_NAME)


.PHONY: deploy
deploy:
	ssh digital-ocean $(DEPLOY_SCRIPT)


DEPLOY_SCRIPT = "\
	docker pull $(ARTIFACT_NAME); \
	docker kill $(NAME); \
	docker rm $(NAME); \
	docker run -d -p 50051:50051 -p 8000:8000 --name $(NAME) $(ARTIFACT_NAME); \
"