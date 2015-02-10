all: compile

compile: bin

bin:
	rm -rf bin
	mkdir bin
	echo "Compiling Backend Server"
	javac -cp "lib/*" -d bin src/*.java
	echo "Done"
run: compile
	echo "Running"
	java -cp "bin/:lib/*" ConnectionTest
	echo "Stopped"
clean:
	rm -rf bin
	
.PHONY: all run clean