javac -cp ./:/home/laboratory/workspace/chea_parallel/commons-math-2.2.jar:/home/laboratory/workspace/chea_parallel/apache-commons-lang.jar:/home/laboratory/workspace/chea_parallel/*.jar:../ sp/CheaSerial.java -d .
jar -cvf CheaSerial.jar .
java -cp CheaSerial.jar:./:/home/laboratory/workspace/chea_parallel/commons-math-2.2.jar:/home/laboratory/workspace/chea_parallel/apache-commons-lang.jar:/home/laboratory/workspace/chea_parallel/*.jar:../ sp.CheaSerial
