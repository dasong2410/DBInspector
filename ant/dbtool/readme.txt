1.jar功能
  dbi.jar
    数据结构对比工具

  dbc.jar
    元数据对比工具

  sqlg.jar
    建表sql生成工具

2.目录
  cfg：配置文件目录
  doc：文档目录
  doc/db：数据库文档目录，存放 需要对比的 或是 需要生成sql语句 的项目数据库文档
  doc/meta：元数据文档目录

3.使用方法（如果报OutOfMemoryError: Java heap space，在加后加上内存设置，java -Xms512m -Xmx1024m -jar dbi.jar）
  java -jar dbi.jar
  java -jar dbc.jar
  java -jar sqlg.jar

注：只支持2007格式文档
