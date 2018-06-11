package net.dasong.common;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class PropsReader {

	public static void read() {
		Map.Entry<?, ?> entry = null;
		Field field = null;

		Properties props = new Properties();

		try {
			// 加载参数文件
			FileReader fr = new FileReader(Constants.CFG_FILE);
			props.load(fr);

			// 获取参数文件中的所有参数
			Set<?> entrySet = props.entrySet();
			Iterator<?> it = entrySet.iterator();

			// 遍历参数，给Constants中对应的变量赋值，此处用到了 反射，主要是方便增加配置文件中的参数
			// 在不改变赋值代码的情况下，增加配置文件中的参数，只需要在Constants中增加相同名称的变量即可
			while (it.hasNext()) {
				entry = (Entry<?, ?>) it.next();

				// 获取 参数 同名的变量
				field = Constants.class.getField((String) entry.getKey());

				// 获取变量类型
				String type = field.getType().getName();

				// 配置文件中一般都应该是int或是String类型，此处只作这两种处理，如果后续还有其它类型再说
				if (type.equals("int")) {
					field.set(Constants.class, Integer.valueOf((String) entry.getValue()));
				} else if (type.equals("java.lang.String")) {
					field.set(Constants.class, entry.getValue());
				}
			}
		} catch (Exception e) {
			// 合并多种异常，不管哪种异常，读取参数文件失败都应该打印错误信息，退出程序
			e.printStackTrace();

			System.exit(1);
		}
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		// 载入参数
		PropsReader.read();

		// 打印参数
		Field[] fields = Constants.class.getFields();

		for (Field field : fields) {

			System.out.println(field.getName() + "=" + field.get(Constants.class));
		}
	}
}
