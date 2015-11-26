javac -cp ./:/home/laboratory/workspace/chea_parallel/commons-math-2.2.jar:/home/laboratory/workspace/chea_parallel/apache-commons-lang.jar:/home/laboratory/workspace/chea_parallel/*.jar:../ chea/chea.java -d .
jar -cvf chea.jar .
java -cp chea.jar:./:/home/laboratory/workspace/chea_parallel/commons-math-2.2.jar:/home/laboratory/workspace/chea_parallel/apache-commons-lang.jar:/home/laboratory/workspace/chea_parallel/*.jar:../ chea.chea
