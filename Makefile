all: compile

compile: bin

bin:
	rm -rf bin
	mkdir bin
	echo "Compiling Backend Server"
	javac -cp "lib/*" -d bin src/core/*.java src/core/json/*.java src/sql/*.java src/sql/wrappers/*.java src/routes/*.java
	echo "Done"
run: compile
	bash /root/server/scripts/server_run
stop:
	bash /root/server/scripts/server_stop
clean:
	rm -rf bin
help:
	echo "VirtualPantry Server"
	echo "\t make run\t\tStart the server"
	echo "\t make stop\t\tStop the server"
	echo "\t make clean\t\tRemove compiled Java files"
	echo "\t make compile\t\tRecompile the server files"
	echo "\tYou must run 'make clean' before recompiling"
	
.PHONY: all run clean
