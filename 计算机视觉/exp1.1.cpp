#include <opencv2/opencv.hpp>
using namespace cv;
Mat srcImage1,dstImage1, dstImage2;
String imagePath = "C:/Users/admin/Pictures/1.1.png";
int main()
{
	//imaread(path,[flag])
	srcImage1 = imread(imagePath, 1);
	imshow("ԭͼ", srcImage1);


	//��ֵ�˲� ������ͼ��Ŀ��ͼ���ں˵Ĵ�С��ê�㣩

	blur(srcImage1, dstImage1, Size(5, 5), Point(-1, -1));
	imshow("��ֵ�˲�(5,5)", dstImage1);

	blur(srcImage1, dstImage1, Size(11, 11), Point(-1, -1));
	imshow("��ֵ�˲�(11, 11)", dstImage1);

	blur(srcImage1, dstImage1, Size(17, 17), Point(-1, -1));
	imshow("��ֵ�˲�(17, 17)", dstImage1);


	//��ֵ�˲� ������ͼ��Ŀ��ͼ���������˲�ģ��ߴ��С[����1������]��
	medianBlur(srcImage1, dstImage2, 5);
	imshow("��ֵ�˲�5", dstImage2);

	medianBlur(srcImage1, dstImage2, 11);
	imshow("��ֵ�˲�11", dstImage2);

	medianBlur(srcImage1, dstImage2, 17);
	imshow("��ֵ�˲�17", dstImage2);

	waitKey(0);
	return 0;
}


