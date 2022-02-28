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
@property (assign, nonatomic) CGFloat totalFlexValue;
@property (assign, nonatomic) BOOL isShowingInstruction;
@property (assign, nonatomic) PanoAnnotationToolOption selectedOption;
@end

@implementation PanoAnnotationTool

- (instancetype)initWithView:(UIView *)parentView toolOption:(PanoAnnotationToolOption)option {
    self = [super init];
    if (self) {
        _isShowingInstruction = true;
        _currentWBType = kPanoWBToolPath;
        _shapeType = PanoShapeLine;
        _option = option;
        _parentView = parentView;
        _fontSizeRange = PanoMakeRange(10, 96);
        _lineWidthRange = PanoMakeRange(1, 20);
        [self initAnnotationTool:_isShowingInstruction];
        _annotationTool.hidden = true;
        _selectedOption = PanoAnnotationToolPath;
    }
    return self;
}

- (void)showToolInstruction {
    _isShowingInstruction = true;
    [self initAnnotationTool:_isShowingInstruction];
    [self updateAnnotationTool];
}

- (void)hideToolInstruction {
    _isShowingInstruction = false;
    [self initAnnotationTool:_isShowingInstruction];
    [self updateAnnotationTool];
}

- (void)updateAnnotationTool {
    _annotationTool.selectedIndex = firstBitFlag(_selectedOption);
}

- (void)initAnnotationTool:(BOOL)showInstruction {
    __weak typeof(self) weakSelf = self;
    NSMutableArray<UIImage *> *normalImages = [NSMutableArray array];
    NSMutableArray<UIImage *> *selectedImages = [NSMutableArray array];
    for (NSString *code in @[@"\U0000e793", @"\U0000e791", @"\U0000e794", @"\U0000e795", @"\U0000e790", @"\U0000e77d", @"\U0000e77f"]) {
        UIImage *normalImage = [UIImage imageWithIconFontSize:36 text:code color:appMainColor()];
        UIImage *selectedImage = [UIImage imageWithIconFontSize:36 text:code color:appHighlightedColor()];
        [normalImages addObject:normalImage];
        [selectedImages addObject:selectedImage];
    }
    PanoAction *selectAction = [[PanoAction alloc] initWithTitle:showInstruction ? NSLocalizedString(@"选择", nil): nil imgIcon:normalImages[0] selectedIcon:selectedImages[0] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolSelect options:@{}];
        [weakSelf.penView dismiss];
    }];
    PanoAction *pathAction = [[PanoAction alloc] initWithTitle:showInstruction ? NSLocalizedString(@"画笔", nil) : nil imgIcon:normalImages[1] selectedIcon:selectedImages[1] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolPath options:@{}];
        [weakSelf toggleAnnotationPenView: PanoAnnotationToolPath ];
    }];
    PanoAction *textAction = [[PanoAction alloc] initWithTitle:showInstruction ? NSLocalizedString(@"文字", nil) : nil imgIcon:normalImages[2] selectedIcon:selectedImages[2] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:kPanoWBToolText options:@{}];
        [weakSelf toggleAnnotationPenView: PanoAnnotationToolText];
    }];
    PanoAction *shapeAction = [[PanoAction alloc] initWithTitle:showInstruction ? NSLocalizedString(@"图形", nil) : nil imgIcon:normalImages[3] selectedIcon:selectedImages[3] handler:^(PanoAction * _Nonnull action) {
        [weakSelf didChooseAnnotationTool:[self wbTool:weakSelf.shapeType] options:@{}];
        [weakSelf toggleAnnotationPenView:PanoAnnotationToolShape];
    }];
    PanoAction *brushAction = [[PanoAction alloc] initWithTitle:showInstruction ? NSLocalizedString(@"删除", nil) : nil imgIcon:normalImages[4] selectedIcon:selectedImages[4] handler:^(PanoAction * _Nonnull action) {
        weakSelf.selectedOption = PanoAnnotationToolBrush;
        [weakSelf didChooseAnnotationTool:kPanoWBToolDelete options:@{}];
        [weakSelf.penView dismiss];
    }];
    PanoAction *undoAction = [[PanoAction alloc] initWithTitle:nil imgIcon:normalImages[5] selectedIcon:nil handler:^(PanoAction * _Nonnull action) {
        [weakSelf.delegate annotationToolDidChoosed:PanoAnnotationToolUndo];
    }];
    undoAction.HighlightedIcon = selectedImages[5];
    
    PanoAction *redoAction = [[PanoAction alloc] initWithTitle:nil imgIcon:normalImages[6] selectedIcon:nil handler:^(PanoAction * _Nonnull action) {
        [weakSelf.delegate annotationToolDidChoosed:PanoAnnotationToolRedo];
    }];
    redoAction.HighlightedIcon = selectedImages[6];
    PanoAction *closeAction = [[PanoAction alloc] initWithTitle:NSLocalizedString(@"关闭白板", nil) imgIcon:nil selectedIcon:nil handler:^(PanoAction * _Nonnull action) {
        [weakSelf.delegate annotationToolDidChoosed:PanoAnnotationToolClose];
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
    if (_option & PanoAnnotationToolUndo && !showInstruction) {
        [items addObject:undoAction];
    }
    if (_option & PanoAnnotationToolRedo && !showInstruction) {
        [items addObject:redoAction];
    }
    if (_option & PanoAnnotationToolClose && !showInstruction) {
        closeAction.flex = 2;
        closeAction.titleColor = [UIColor pano_colorWithHexString:@"#EF476B"];
        [items addObject:closeAction];
    }
    _totalFlexValue = 0.0;
    for (PanoAction *action in items) {
        _totalFlexValue += (CGFloat)action.flex;
    }
    [_annotationTool removeFromSuperview];
    _annotationTool = nil;
    _annotationTool = [[PanoAnnotationToolView alloc] initWithItems:items];
    _annotationTool.backgroundColor = [UIColor pano_colorWithHexString:@"#F4F4F4"];
    [_parentView addSubview:_annotationTool];
    CGFloat bottom = isIphoneX() ? bottomSafeValue() : 0;
    CGFloat height = (showInstruction ? 70 : 50) + bottom;
    CGFloat width = MIN(CGRectGetWidth(_parentView.frame), CGRectGetHeight(_parentView.frame));
    [_annotationTool mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(height);
        make.bottom.centerX.equalTo(_parentView);
        showInstruction ? make.left.right.equalTo(_parentView) :
        make.width.mas_equalTo(width);
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
        _selectedOption = option;
        CGFloat ratio = 0.3;
        CGFloat midValue = 1.0 / (_totalFlexValue * 2);
        switch (option) {
            case PanoAnnotationToolPath:
                ratio = 1.0 / _totalFlexValue + midValue;
                break;
            case PanoAnnotationToolText:
                ratio = 2.0 / _totalFlexValue + midValue;
                break;
            case PanoAnnotationToolShape:
                ratio = 3.0 / _totalFlexValue + midValue;
                break;
            default:
                ratio = 0.3;
                break;
        }
        CGFloat p = CGRectGetWidth(_annotationTool.bounds) * ratio + CGRectGetMinX(_annotationTool.frame);
        _penView = [PanoAnnotationPenView showWithParentView:_parentView arrowPositionScreenX:p toolOption:option colors: [PanoCallClient shared].config.penColors shapes:self.shapes];
        PanoConfig *config = PanoCallClient.shared.config;
        _penView.delegate = self;
        _penView.bgColor = _annotationTool.backgroundColor;
        _penView.activeShape = _shapeType;
        if (_currentWBType == kPanoWBToolPath) {
            _penView.range = _lineWidthRange;
        } else if (_currentWBType == kPanoWBToolText) {
            _penView.range = _fontSizeRange;
        }
        if (_option & PanoAnnotationToolWhiteBoardFlag) {
            _penView.activeColor = config.wbColor;
            _penView.lineWidth = _currentWBType == kPanoWBToolPath ? config.wbLineWidth:
                config.wbFontSize;
        } else {
            _penView.activeColor = config.annoColor;
            _penView.lineWidth = _currentWBType == kPanoWBToolPath ?        config.annoLineWidth:
                config.annoFontSize;
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
    [self updateAnnotationTool];
}

- (void)hide {
    [_annotationTool hide];
    [_penView dismiss];
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
    } else if (_currentWBType == kPanoWBToolText){
        options = @{PanoToolKeyFontSize: @(width)};
    }
    NSLog(@"lineWidth-> %ld, %u", (long)_currentWBType, (unsigned int)width);
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
        shape ^= PanoShapeFillFlag; // 取反
    }
    return (PanoWBToolType)shape;
}

@end
