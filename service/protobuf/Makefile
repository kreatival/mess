
all:
	protoc --java_out=../src/main/java/ TextSecure.proto PubSubMessage.proto Directory.proto Groups.proto

clean:
	rm -f ../src/main/java/su/sres/shadowserver/entities/MessageProtos.java
	rm -f ../src/main/java/su/sres/shadowserver/groups/protos/*
	rm -f ../src/main/java/su/sres/shadowserver/storage/PubSubProtos.java
	rm -f ../src/main/java/su/sres/shadowserver/storage/protos/*

