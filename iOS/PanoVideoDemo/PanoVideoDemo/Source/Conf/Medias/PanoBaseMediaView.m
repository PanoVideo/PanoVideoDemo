//
//  PanoBaseMediaView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseMediaView.h"
#import "PanoCallClient.h"

PanoMediaInfoKey const PanoTopToolBarState = @"PanoTopToolBarState";
PanoMediaInfoKey const PanoNetworkStatus = @"PanoNetworkStatus";
PanoMediaInfoKey const PanoMediaViewPostionKey = @"PanoMediaViewPostionKey";
PanoMediaInfoKey const PanoFloatViewPostionKey = @"PanoFloatViewPostionKey";
@implementation PanoBaseMediaView {
    UITapGestureRecognizer *_doubleTapGesture;
    UIView *_coverView;
}

- (void)initViews {
    _contentView = [[UIView alloc] init];
    [self addSubview:_contentView];
    
    _coverView =  [[UIView alloc] init];
    _coverView.hidden = true;
    [self addSubview:_coverView];
    
    UITapGestureRecognizer *doubleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(doubleClickAction:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    doubleTapGesture.numberOfTouchesRequired  = 1;
    [_coverView addGestureRecognizer:doubleTapGesture];
    _doubleTapGesture = doubleTapGesture;
    
    _annotationView = [[UIView alloc] init];
    _annotationView.hidden = true;
    [self addSubview:_annotationView];
}

- (void)setEnableDoubleClick:(BOOL)enableDoubleClick {
    _coverView.hidden = !enableDoubleClick;
}

- (void)initConstraints {
    [_contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
    [_coverView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
    [_annotationView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
}

- (void)doubleClickAction:(id)gesture {
    if (self.doubleClickBlock) {
        self.doubleClickBlock(self.instance);
    }
}

- (void)setActive:(BOOL)active {
    _active = active;
}

- (void)start {
    
}

- (void)stop {
    
}

- (void)update:(NSDictionary<PanoMediaInfoKey,id> *)info {
    
}

@end
