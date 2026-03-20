rm -rf out
mkdir -p out
javac --release 21 -encoding UTF-8 -d out $(find src/battleship -name "*.java")
echo "Compilado todo"
sleep 1
clear