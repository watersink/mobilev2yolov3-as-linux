//
// Created by root on 6/3/19.
//

#ifndef YOLOV3_H
#define YOLOV3_H


#include <iostream>
#include <vector>
using namespace std;

#include "mobilenetv2_yolov3.id.h"
#include "net.h"




class YOLOV3 {

public:
    YOLOV3(string param_path, string bin_path);
	YOLOV3(ncnn::Mat param_path, ncnn::Mat bin_path);

	ncnn::Mat detect(ncnn::Mat in);

    ~YOLOV3();

private:

    ncnn::Net yolov3_net;

    const int target_size = 352;
    const float mean_vals[3] = {127.5f, 127.5f, 127.5f};
    const float norm_vals[3] = {0.007843f, 0.007843f, 0.007843f};

};


#endif //YOLOV3_H
