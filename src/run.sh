./auto_serial.sh
sed -i -e 's|DTLZ1|DTLZ2|' chea/chea.java
./auto_serial.sh
sed -i -e 's|DTLZ2|DTLZ3|' chea/chea.java
./auto_serial.sh
sed -i -e 's|DTLZ3|DTLZ4|' chea/chea.java
./auto_serial.sh
sed -i -e 's|DTLZ4|DTLZ1|' chea/chea.java
