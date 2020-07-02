local: local-deploy-containers

.PHONY: sync-envoy
sync-envoy:
	rsync -zvh \
		./build/generated/source/proto/main/descriptor_set.desc \
		./deploy/envoy/config.linux.yml \
		root@digital-ocean:/var/www/fuel_hunter/config

.PHONY: local-deploy-containers
local-deploy-containers:
	docker-compose -f ./deploy/docker-compose.yml up -d

# TODO: use ansible?
#.PHONY: prod-deploy-containers
#prod-deploy-containers:
#	eval "$(docker-machine env digital-ocean)"; \
#	docker-compose -f ./deploy/docker-compose.yml -f ./deploy/docker-compose.prod.yml up -d --build; \
#	eval "$(docker-machine env -u)"
