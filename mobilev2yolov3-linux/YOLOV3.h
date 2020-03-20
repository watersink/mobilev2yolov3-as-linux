//
// Created by root on 6/3/19.
//

#ifndef YOLOV3_H
#define YOLOV3_H


#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <iostream>
#include <vector>
using namespace std;
using namespace cv;

#include "mobilenetv2_yolov3.id.h"
#include "net.h"


struct Object
{
    float x1,y1,x2,y2;
    int label;
    float prob;
};



class YOLOV3 {

public:
    YOLOV3(string param_path, string bin_path);

    vector<Object> detect(const cv::Mat &bgr);
	cv::Mat draw_objects(const cv::Mat& bgr, const std::vector<Object>& objects);

    ~YOLOV3();

private:

    ncnn::Net yolov3_net;

    const int target_size = 352;
    const float mean_vals[3] = {127.5f, 127.5f, 127.5f};
    const float norm_vals[3] = {0.007843f, 0.007843f, 0.007843f};

};


#endif //YOLOV3_H
