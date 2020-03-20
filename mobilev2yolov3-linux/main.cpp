#include <stdio.h>

#include "YOLOV3.h"

#include "platform.h"
#include "net.h"
#if NCNN_VULKAN
#include "gpu.h"
#endif // NCNN_VULKAN



int main(int argc, char** argv)
{



    YOLOV3 yolov3("../assets/mobilenetv2_yolov3.param.bin","../assets/mobilenetv2_yolov3.bin");
    string imagepath="../dog.jpg";

    cv::Mat bgr = cv::imread(imagepath, 1);
    std::vector<Object> objects=yolov3.detect(bgr);

	cv::Mat image = yolov3.draw_objects(bgr, objects );


	cv::imwrite("det.jpg",image);
    //cv::imshow("image", image);
    //cv::waitKey(0);

    return 0;
}
