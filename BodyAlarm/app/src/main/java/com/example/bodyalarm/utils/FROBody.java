package com.example.bodyalarm.utils;

public class FROBody {
	private static final String TAG="FROBody";

	/**
	 * 判断是否设防
	 * @param rightLen
	 * @param nodeNum
	 * @return
	 */
	public static Boolean isOn(int rightLen, int nodeNum,byte[] read_buff) {

		Boolean statu = null;
		float data=0;
			if (read_buff!=null) {
				// 长度是否正确，节点号是否正确，CRC是否正确
				if ((read_buff.length == rightLen && read_buff[0] == nodeNum) && CRCValidate.isCRCConfig(read_buff)) {
					/******************** CRC校验正确之后做的，解析数据 ********************/
					// 参数（要拷贝的数组源，拷贝的开始位置，要拷贝的目标数组，填写的开始位置，拷贝的长度）
					byte[] data_buff = new byte[2];//存放数据数组
					//设防开始位,第四位开始
					int dataOffset=3;
					//抠出数据，放进data_buff
					System.arraycopy(read_buff, dataOffset, data_buff, 0, 1);
					//解析数据data_buff（16进制转10进制）
					data= ByteToFloatUtil.hBytesToFloat(data_buff);
//					// 十六进制转化为十进制，结果140。
//					Integer.parseInt("8c",16);
					/*********根据数据返回真假**********/
					statu =(data==0.0)?false:true;
					return statu;
				}
			}
		return statu;// 返回数据
	}

	/**
	 * 解析单数据型数据
	 * @param rightLen
	 * @param nodeNum
	 * @return
	 */
	public static Boolean getData(int rightLen, int nodeNum,byte[] read_buff) {

		Boolean statu = null;
		float data=0;
		if (read_buff!=null) {
			// 长度是否正确，节点号是否正确，CRC是否正确
			if ((read_buff.length == rightLen && read_buff[0] == nodeNum) && CRCValidate.isCRCConfig(read_buff)) {
				/******************** CRC校验正确之后做的，解析数据 ********************/
				// 参数（要拷贝的数组源，拷贝的开始位置，要拷贝的目标数组，填写的开始位置，拷贝的长度）
				byte[] data_buff = new byte[2];//存放数据数组
				//数据开始位,第五位开始
				int dataOffset=4;
				//抠出数据，放进data_buff
				System.arraycopy(read_buff, dataOffset, data_buff, 0, 1);
				//解析数据data_buff（16进制转10进制）
				data= ByteToFloatUtil.hBytesToFloat(data_buff);
				/*********根据数据返回真假**********/
				if (data==0.0) {
					statu = false;
					return statu;
				}
				else {
					statu = true;
					return statu;
				}
			}
		}
		return statu;// 返回数据
	}

}
