.PHONY: all run build bench-aws bench-aws-full verify clean

all: clean build bench-aws-full

run:
	clj -M -m related.core

build:
	clj -T:build uber

jar: build
	java -jar target/related.jar

bench-aws:
	clj -J-XX:ActiveProcessorCount=4 -J-Xmx8g -M -m related.core

bench-aws-full:
	cd .. && ./bench_local_aws_sim.sh techascent_con 3

verify:
	cd .. && python verify.py related_posts_techascent_con.json

clean:
	rm -rf target .cpcache
