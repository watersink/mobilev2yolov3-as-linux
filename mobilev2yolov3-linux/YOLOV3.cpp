//
// Created by root on 6/3/19.
//

#include "YOLOV3.h"


YOLOV3::YOLOV3(string param_path, string bin_path) {

    const char *param_path_char = param_path.c_str();
    const char *bin_path_char = bin_path.c_str();


    int ret_param = yolov3_net.load_param_bin(param_path_char);
    int ret_bin = yolov3_net.load_model(bin_path_char);

	std::cout<<"### "<<ret_param<<" "<<ret_bin<<std::endl;


}


vector<Object> YOLOV3::detect(const cv::Mat &bgr) {

    int img_w = bgr.cols;
    int img_h = bgr.rows;

    ncnn::Mat in = ncnn::Mat::from_pixels_resize(bgr.data, ncnn::Mat::PIXEL_BGR, bgr.cols, bgr.rows, target_size, target_size);


    in.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Extractor ex = yolov3_net.create_extractor();
    ex.set_num_threads(2);

    ex.input(mobilenetv2_yolov3_param_id::BLOB_data, in);

    ncnn::Mat out;
    ex.extract(mobilenetv2_yolov3_param_id::BLOB_detection_out, out);

    std::vector<Object> objects;
    for (int i=0; i<out.h; i++)
    {
        const float* values = out.row(i);

        Object object;
        object.label = values[0];
        object.prob = values[1];
        object.x1 = values[2] * img_w;
        object.y1 = values[3] * img_h;
        object.x2 = values[4] * img_w;
        object.y2 = values[5] * img_h;

        objects.push_back(object);
    }




    return objects;
}




cv::Mat YOLOV3::draw_objects(const cv::Mat& bgr, const std::vector<Object>& objects)
{
    static const char* class_names[] = {"background",
        "aeroplane", "bicycle", "bird", "boat",
        "bottle", "bus", "car", "cat", "chair",
        "cow", "diningtable", "dog", "horse",
        "motorbike", "person", "pottedplant",
        "sheep", "sofa", "train", "tvmonitor"};


    cv::Mat image = bgr.clone();
    cout<<objects.size()<<endl;
    for (size_t i = 0; i < objects.size(); i++)
    {
        const Object& obj = objects[i];

        fprintf(stderr, "%d = %.5f at %.2f %.2f %.2f x %.2f\n", obj.label, obj.prob,
                obj.x1, obj.y1, obj.x2, obj.y2);

        cv::rectangle(image, cv::Point(obj.x1,obj.y1),cv::Point(obj.x2,obj.y2), cv::Scalar(0, 0, 255),3);

        char text[256];
        sprintf(text, "%s %.1f%%", class_names[obj.label], obj.prob * 100);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

        int x = obj.x1;
        int y = obj.y1 - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > image.cols)
            x = image.cols - label_size.width;

        cv::rectangle(image, cv::Rect(cv::Point(x, y),
                                      cv::Size(label_size.width, label_size.height + baseLine)),
                      cv::Scalar(255, 255, 255), -1);

        cv::putText(image, text, cv::Point(x, y + label_size.height),
                    cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));
    }

	return image;
}



YOLOV3::~YOLOV3() {

}
