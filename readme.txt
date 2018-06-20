1.jar功能
  dbi.jar
    数据结构对比工具，对比docx文档中的表结构与数据库里是否一致，将不一致的信息打印出来

  sqlg.jar
    建表sql生成工具，将docx文档里的表结构生成 建表 sql

2.目录
  cfg：配置文件目录
  doc：文档目录
  doc/db：数据库文档目录，存放 需要对比的 或是 需要生成sql语句 的项目数据库文档

3.使用方法（如果报OutOfMemoryError: Java heap space，在加后加上内存设置，java -Xms512m -Xmx1024m -jar dbi.jar）
  java -jar dbi.jar
  java -jar sqlg.jar

注：只支持2007格式文档
