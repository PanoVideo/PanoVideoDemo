//
//  PanoAnnotationTool.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAnnotationTool.h"
#import "PanoCallClient.h"
#import "UIImage+IconFont.h"

PanoAnnotationToolKey PanoToolKeyColor = @"PanoToolKeyColor";
PanoAnnotationToolKey PanoToolKeyLineWidth = @"PanoToolKeyLineWidth";
PanoAnnotationToolKey PanoToolKeyFontSize = @"PanoToolKeyFontSize";
PanoAnnotationToolKey PanoToolKeyFill = @"PanoToolKeyFill";

@interface PanoAnnotationTool () <PanoAnnotationPenViewDelegate>
@property (weak, nonatomic) UIView *parentView;
@property (assign, nonatomic) NSUInteger annotationLineWidth;
@property (assign, nonatomic) PanoWBToolType currentWBType;
@property (assign, nonatomic) PanoShapeType shapeType;
@end

@implementation PanoAnnotationTool

- (instancetype)initWithView:(UIView *)parentView toolOption:(PanoAnnotationToolOption)option {
    self = [super init];
    if (self) {
        _currentWBType = kPanoWBToolPath;
        _shapeType = PanoShapeLine;
        _option = option;
        _parentView = parentView;
        _fontSizeRange = PanoMakeRange(1, 20);
        _lineWidthRange = PanoMakeRange(1, 20);
        [self initAnnotationTool];
    }
    return self;
}

- (void)initAnnotationTool {
    __weak typeof(self) weakSelf = self;
    NSMutableArray<UIImage *> *normalImages = [NSMutableArray array];
    NSMutableArray<UIImage *> *selectedImages = [NSMutableArray array];
    for (NSString *code in @[@"\U0000e793", @"\U0000e791", @"\U0000e794", @"\U0000e795", @"\U0000e790"]) {
        UIImage *normalImage = [UIImage imageWithIconFontSize:36 text:code color:appMainColor()];
        UIImage *selectedImage = [UIImage imageWithIconFontSize:36 text:code color:appHighlightedColor()];
        [normalImages addObject:normalImage];
        [selectedImages addObject:selectedImage];
    }
    PanoAction *selectAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"选择", nil) imgIcon:normalImages[0] selectedIcon:selectedImages[0] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolSelect options:@{}];
        [weakSelf.penView dismiss];
    }];
    PanoAction *pathAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"画笔", nil) imgIcon:normalImages[1] selectedIcon:selectedImages[1] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolPath options:@{}];
        [weakSelf toggleAnnotationPenView: PanoAnnotationToolPath ];
    }];
    PanoAction *textAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"文字", nil) imgIcon:normalImages[2] selectedIcon:selectedImages[2] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolText options:@{}];
        [weakSelf toggleAnnotationPenView: PanoAnnotationToolText];
    }];
    PanoAction *shapeAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"图形", nil) imgIcon:normalImages[3] selectedIcon:selectedImages[3] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:[self wbTool:weakSelf.shapeType] options:@{}];
        [weakSelf toggleAnnotationPenView:PanoAnnotationToolShape];
    }];
    PanoAction *brushAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"删除", nil) imgIcon:normalImages[4] selectedIcon:selectedImages[4] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolEraser options:@{}];
        [weakSelf.penView dismiss];
    }];
    NSMutableArray *items = [NSMutableArray array];
    if (_option & PanoAnnotationToolSelect) {
        [items addObject:selectAction];
    }
    if (_option & PanoAnnotationToolPath) {
        [items addObject:pathAction];
    }
    if (_option & PanoAnnotationToolText) {
        [items addObject:textAction];
    }
    if (_option & PanoAnnotationToolShape) {
        [items addObject:shapeAction];
    }
    if (_option & PanoAnnotationToolBrush) {
        [items addObject:brushAction];
    }
    _annotationTool = [[PanoAnnotationToolView alloc] initWithItems:items];
    _annotationTool.hidden = true;
    _annotationTool.backgroundColor = [UIColor pano_colorWithHexString:@"#F4F4F4"];
    [_parentView addSubview:_annotationTool];
    CGFloat bottom = pano_safeAreaInset(UIApplication.sharedApplication.keyWindow).bottom;
    [_annotationTool mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(70 + bottom);
        make.left.right.bottom.equalTo(_parentView);
    }];
}

- (void)didChooseAnnotationTool:(PanoWBToolType)type options:(NSDictionary *)options{
    _currentWBType = type;
    [self.delegate annotationToolDidChooseType:type options:options];
}

- (void)toggleAnnotationPenView:(PanoAnnotationToolOption)option {
    if (self.penView && [self.penView isShowing]) {
        [self.penView dismiss];
    } else {
        CGFloat ratio = 0.3;
        switch (option) {
            case PanoAnnotationToolPath:
                ratio = 0.3;
                break;
            case PanoAnnotationToolText:
                ratio = 0.5;
                break;
            case PanoAnnotationToolShape:
                ratio = 0.7;
                break;
            default:
                ratio = 0.3;
                break;
        }
        CGFloat p = CGRectGetWidth(_parentView.bounds) * ratio;
        _penView = [PanoAnnotationPenView showWithParentView:_parentView arrowPositionScreenX:p toolOption:option colors: [PanoCallClient sharedInstance].config.penColors shapes:self.shapes];
        PanoConfig *config = PanoCallClient.sharedInstance.config;
        _penView.delegate = self;
        _penView.bgColor = _annotationTool.backgroundColor;
        _penView.activeShape = _shapeType;
        if (_option & PanoAnnotationToolWhiteBoardFlag) {
            _penView.activeColor = config.wbColor;
            _penView.lineWidth = _currentWBType == kPanoWBToolPath ? config.wbLineWidth:
                config.wbFontSize;
        } else {
            _penView.activeColor = config.annoColor;
            _penView.lineWidth = _currentWBType == kPanoWBToolPath ?        config.annoLineWidth:
                config.annoFontSize;
        }
        if (_currentWBType == kPanoWBToolPath) {
            _penView.range = _lineWidthRange;
        } else if (_currentWBType == kPanoWBToolText) {
            _penView.range = _fontSizeRange;
        }
    }
}

- (NSArray *)shapes {
    NSMutableArray *data = [@[@[@"\U0000e78a", @(PanoShapeLine)],
                              @[@"\U0000e78b", @(PanoShapeEllipse)],
                              @[@"\U0000e78c", @(PanoShapeRect)]] mutableCopy];
    if (_option & PanoAnnotationToolWhiteBoardFlag) {
        [data addObjectsFromArray:@[@[@"\U0000e78d", @(PanoShapeFillEllipse)],
                                    @[@"\U0000e78e", @(PanoShapeFillRect)]]];
    }
    return data;
}

- (void)show {
    [_annotationTool show];
}

- (void)hide {
    [_annotationTool hide];
}

- (BOOL)isVisible {
    return [_annotationTool isVisible];
}

#pragma mark -- PanoAnnotationColorDelegate

- (void)annotationColorViewDidChooseColor:(nonnull UIColor *)color {
    [_delegate annotationToolDidChooseType:_currentWBType options:@{PanoToolKeyColor: color}];
}

- (void)annotationViewDidChooseLineWidth:(UInt32)width {
    NSDictionary *options = nil;
    if (_currentWBType == kPanoWBToolPath) {
        options = @{PanoToolKeyLineWidth : @(width)};
    } else {
        options = @{PanoToolKeyFontSize: @(width)};
    }
    [_delegate annotationToolDidChooseType:_currentWBType options:options];
}

- (void)annotationViewDidChooseShape:(PanoShapeType)shape {
    _shapeType = shape;
    NSNumber *fill = @(false);
    if (shape == PanoShapeFillEllipse || shape == PanoShapeFillRect ) {
        fill = @(true);
    }
    [self didChooseAnnotationTool:[self wbTool:shape] options:@{PanoToolKeyFill : fill}];
}

- (PanoWBToolType)wbTool:(PanoShapeType)shape {
    if (shape == PanoShapeFillEllipse || shape == PanoShapeFillRect ) {
        shape ^= PanoShapeFillFlag;
    }
    return (PanoWBToolType)shape;
}

@end
