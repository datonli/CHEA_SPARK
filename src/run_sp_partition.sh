./auto_spark_chea_part.sh
sed -i -e 's|DTLZ1|DTLZ2|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ2|DTLZ3|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ3|DTLZ4|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ4|DTLZ1|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum_2|partitionNum_4|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum = 2|partitionNum = 4|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ1|DTLZ2|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ2|DTLZ3|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ3|DTLZ4|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ4|DTLZ1|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum_4|partitionNum_8|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum = 4|partitionNum = 8|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ1|DTLZ2|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ2|DTLZ3|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ3|DTLZ4|' sp/CheaSpPartition.java
./auto_spark_chea_part.sh
sed -i -e 's|DTLZ4|DTLZ1|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum_8|partitionNum_2|' sp/CheaSpPartition.java
sed -i -e 's|partitionNum = 8|partitionNum = 2|' sp/CheaSpPartition.java
