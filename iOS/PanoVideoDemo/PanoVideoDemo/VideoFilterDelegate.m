//
//  VideoFilterDelegate.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "VideoFilterDelegate.h"
//#import "libCNamaSDK/FURenderer.h"
//#import "authpack.h"

@interface VideoFilterDelegate ()

@property (assign, nonatomic) int item;
@property (assign, nonatomic) int count;
@property (assign, nonatomic) PanoVideoRotation rotation;

@end

@implementation VideoFilterDelegate

+ (void)initialize {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
//        [[FURenderer shareRenderer] setupWithDataPath:nil authPackage:&g_auth_package authSize:sizeof(g_auth_package) shouldCreateContext:YES];
    });
}

- (instancetype)init {
    self = [super init];
    if (nil != self) {
        /*
        NSString * path = nil;
        path = [NSBundle.mainBundle pathForResource:@"ai_face_processor"
                                             ofType:@"bundle"];
        NSData * ai_face_processor = [NSData dataWithContentsOfFile:path];
        [FURenderer loadAIModelFromPackage:(void*)ai_face_processor.bytes
                                      size:(int)ai_face_processor.length
                                    aitype:FUAITYPE_FACEPROCESSOR];
        path = [NSBundle.mainBundle pathForResource:@"face_beautification"
                                             ofType:@"bundle"];
        _item = [FURenderer itemWithContentsOfFile:path];
        [FURenderer itemSetParam:_item withName:@"filter_name" value:@"origin"];
        [FURenderer itemSetParam:_item withName:@"color_level" value:@(1.0)];
        [FURenderer itemSetParam:_item withName:@"face_shape" value:@(2.0)];
        [FURenderer itemSetParam:_item withName:@"face_shape_level" value:@(1.0)];
        _count = 0;
        _rotation = kPanoRotation90;
        fuSetDefaultRotationMode(3);*/
    }
    return self;
}

- (void)dealloc {
    /*
    [FURenderer releaseAIModel:FUAITYPE_FACEPROCESSOR];
    [FURenderer destroyAllItems];
    _item = 0;*/
}

- (void)setBeautifyIntensity:(Float32)intensity {
//    [FURenderer itemSetParam:_item withName:@"blur_level" value:@(intensity * 6)];
}

- (void)setCheekThinningIntensity:(Float32)intensity {
//    [FURenderer itemSetParam:_item withName:@"cheek_thinning" value:@(intensity)];
}

- (void)setEyeEnlargingIntensity:(Float32)intensity {
//    [FURenderer itemSetParam:_item withName:@"eye_enlarging" value:@(intensity)];
}

- (void)onNativeVideoFrame:(CVPixelBufferRef _Nonnull)frame
              withRotation:(PanoVideoRotation)rotation {
//    [self updateVideoRotation:rotation];
//    [FURenderer.shareRenderer renderPixelBuffer:frame withFrameId:_count items:&_item itemCount:1];
//    _count++;
}

- (void)updateVideoRotation:(PanoVideoRotation)rotation {
//    if (_rotation != rotation) {
//#if defined(DEBUG)
//        NSLog(@"Video frame rotation change to %ld", (long)rotation);
//#endif
//        _rotation = rotation;
//        switch (rotation) {
//            case kPanoRotation0:
//                fuSetDefaultRotationMode(0);
//                break;
//            case kPanoRotation90:
//                fuSetDefaultRotationMode(3);
//                break;
//            case kPanoRotation180:
//                fuSetDefaultRotationMode(2);
//                break;
//            case kPanoRotation270:
//                fuSetDefaultRotationMode(1);
//                break;
//            default:
//                break;
//        }
//    }
}

@end
