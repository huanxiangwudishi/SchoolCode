#include <opencv2/opencv.hpp>
using namespace cv;
using namespace std;
Mat srcImage, dstImage1, dstImage2;
int h_time = 1, s_time = 1, v_time = 1;
String imagePath = "C:/Users/admin/Pictures/car2.jpg";
int main() {
	srcImage = imread(imagePath, 1);
	imshow("ԭͼ", srcImage);

	// ��˹ģ��
	Mat gausImage = srcImage.clone();
	GaussianBlur(srcImage, gausImage, Size(3, 3), 0, 0);
	imshow("��˹ģ��", gausImage);

	// �ҶȻ�
	Mat grayImage = srcImage.clone();
	cvtColor(gausImage, grayImage, COLOR_BGR2GRAY);
	 imshow("��ͼ", grayImage);

	// Sobel����
	Mat sobelImage = srcImage.clone();
	Sobel(grayImage, sobelImage, CV_8UC1, 1, 0);
	imshow("Sobel����", sobelImage);

	// ��ֵ��
	Mat binaryImage = srcImage.clone();
	threshold(sobelImage, binaryImage, 90, 255, THRESH_BINARY);
	imshow("��ֵ��", binaryImage);

	// ������(�ȸ�ʴ��������)
//		Mat morphologyExImage=srcImage.clone();
//		Imgproc.morphologyEx(binaryImage, morphologyExImage, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement( Imgproc.MORPH_RECT,new Size(1,1)));
//		HighGui.imshow("������", morphologyExImage);

	Mat erodeImage = srcImage.clone();
	erode(binaryImage, erodeImage, getStructuringElement(MORPH_RECT, Size(3, 2)));
	imshow("��ʴ", erodeImage);

	Mat dilateImage = srcImage.clone();
	dilate(erodeImage, dilateImage, getStructuringElement(MORPH_RECT, Size(22, 22)));
	imshow("����", dilateImage);

	
	// ȡ����,��ʾȫ������
	Mat flagImage = srcImage.clone();
	vector<vector<Point>> contours;
	Mat hierarchy ;
	findContours(dilateImage, contours, RETR_EXTERNAL, CHAIN_APPROX_NONE);
	drawContours(flagImage, contours, -1, Scalar(0, 255, 0));//���߻������е�����
	imshow("����", flagImage);


	//ʶ����(�ҳ��������ҿ�߱�������Ҫ��)�������ṩ����������ʶ����Ч!!!
	double maxArea = 0;
	int targetIndex;
	for (int i = 1; i < contours.size(); i++) {
		double area = contourArea(contours[i]);
		Rect rect = boundingRect(contours[i]);
		double scaleWH = (double)rect.width / rect.height;//���������Χ��Ŀ���
		if (area > maxArea&&scaleWH > 1.5 && scaleWH < 3.5) {
			maxArea = area;
			targetIndex = i;
		}
	}

	//��ʾ��������
	Mat licenseImage = srcImage.clone();
	Rect r = boundingRect(contours[targetIndex]);
	rectangle(licenseImage,r,Scalar(0, 255, 0));
	imshow("���Ʊ��", licenseImage);

	waitKey(0);
	return 0;
 }