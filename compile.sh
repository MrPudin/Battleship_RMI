rm -rf out
mkdir -p out
/usr/lib/jvm/java-21-openjdk-amd64/bin/javac --release 21 -encoding UTF-8 -d out $(find src/battleship -name "*.java")
echo "Compilado todo"
sleep 1
clear