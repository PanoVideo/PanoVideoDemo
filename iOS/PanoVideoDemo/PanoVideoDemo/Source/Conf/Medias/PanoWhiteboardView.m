//
//  PanoWhiteBoradView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoWhiteboardView.h"
#import "PanoCallClient.h"
#import "PanoWbTopView.h"
#import "PanoItem.h"
#import "PanoArrayDataSource.h"
#import "PanoMenuCell.h"
#import "PanoAnnotationTool.h"
#import "UIImage+IconFont.h"
#import "PanoDefine.h"
#import "PanoVideoView.h"

@interface PanoWhiteboardView()
<PanoRtcWhiteboardDelegate,
 PanoWhiteboardDelegate,
 PanoAnnotationToolDelegate,
 PanoPoolActiveAudioDelegate>
@property (strong, nonatomic) UIView * drawView;
@property (strong, nonatomic) PanoWbTopView *topBarView;
@property (strong, nonatomic) PanoArrayDataSource *moreItems;
@property (strong, nonatomic) PanoAnnotationTool* annotationTool;
@property (strong, nonatomic) UIView *topContentView;
@property (strong, nonatomic) UIStackView *rightTopView;
@property (strong, nonatomic) UIButton *backButton;
@property (strong, nonatomic) UIButton *rarotionButton;

@property (strong, nonatomic) PanoWBVisionConfig *visionConfig;
@property (weak, nonatomic)   PanoRtcWhiteboard * whiteboardEngine;
@property (assign, nonatomic) PanoWBPageNumber curPage;
@property (assign, nonatomic) UInt32 totalPages;

@end

@implementation PanoWhiteboardView

- (void)initViews {
    
    _enable = true;
    
    self.backgroundColor = [UIColor blackColor];
    
    self.whiteboardEngine = PanoCallClient.shared.engineKit.whiteboardEngine;
    
    [self.whiteboardEngine setLineWidth:self.config.wbLineWidth];
    // 1. 添加白板的容器
    _drawView = [[UIView alloc] init];
    [self addSubview:_drawView];
    // 2. 添加旋转按钮
    [self bringSubviewToFront:self.rarotionButton];
    // 3. 添加顶部工具栏
    [self initTopView];
    // 4. 添加标注工具栏
    [self initAnnotationTool];
    // 5. 初始化白板
    [self initWhiteboard];
    // 6. 打开白板
    [self openWhiteboard];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(statusBarOrientationDidChange) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
    [self statusBarOrientationDidChange];
}

- (void)statusBarOrientationDidChange {
    [_drawView resignFirstResponder];
    [_annotationTool.penView dismiss];
    [self updateWbViewConstraints];
    BOOL islandcape = isLandscape();
    _rarotionButton.selected = islandcape;
    if (!_enable) {
        return;
    }
    [UIView animateWithDuration:0.25 animations:^{
        CGFloat alpha = islandcape ? 0.0 : 1.0;
        self.rightTopView.alpha = alpha;
        self.backButton.alpha = alpha;
        self.topContentView.backgroundColor = islandcape ? [UIColor clearColor] : appMainColor();
        islandcape ? [self.annotationTool hideToolInstruction] : [self.annotationTool showToolInstruction];
    }];
}

- (PanoConfig *)config {
    return PanoCallClient.shared.config;
}

- (PanoWBVisionConfig *)visionConfig {
    if (!_visionConfig) {
        _visionConfig = [[PanoWBVisionConfig alloc] init];
        _visionConfig.width = 1600;
        _visionConfig.height = 900;
        _visionConfig.limited = true;
    }
    return _visionConfig;
}

- (void)rotationScreen:(UIButton *)sender {
    sender.selected = !sender.selected;
    [self setScreenLandscape:sender.selected];
}

- (void)setScreenLandscape:(BOOL)landscape {
    UIInterfaceOrientation orientation = landscape ? UIInterfaceOrientationLandscapeRight : UIInterfaceOrientationPortrait;
    NSNumber *orientationTarget = @(orientation);
    [[UIDevice currentDevice] setValue:orientationTarget forKey:@"orientation"];
    [UIViewController attemptRotationToDeviceOrientation];
}

/// 更新白板的约束，为了横竖屏维持 16: 9 的比例
- (void)updateWbViewConstraints {
    CGFloat radio = 16.0 / 9.0;
    CGFloat screenWidth = MIN(UIScreen.mainScreen.bounds.size.width, UIScreen.mainScreen.bounds.size.height);
    CGFloat screenHeight = MAX(UIScreen.mainScreen.bounds.size.width, UIScreen.mainScreen.bounds.size.height);
    CGFloat screenRadio = screenHeight/screenWidth;
    // 竖屏的时候: 屏幕宽度 / config 宽度
    CGFloat scaleValue = screenWidth / self.visionConfig.width;
    if (isLandscape()) {
        if (screenRadio < radio) { // 横屏的时候 iPad的比例
            scaleValue = screenHeight / self.visionConfig.width;
        } else {  // 横屏的时候 iPhone的比例
            scaleValue = screenWidth / self.visionConfig.height;
        }
    }
    [self.whiteboardEngine setCurrentScaleFactor:scaleValue];
    [self.drawView mas_remakeConstraints:^(MASConstraintMaker *make) {
        if (!isLandscape() || screenRadio < radio) {
            // 竖屏 || 屏幕的宽高比 < (16 : 9)
            make.left.right.centerY.mas_equalTo(self);
            make.height.mas_equalTo(self.drawView.mas_width).dividedBy(radio);
        } else { // 横屏
            make.top.bottom.centerX.mas_equalTo(self);
            make.width.mas_equalTo(self.drawView.mas_height).multipliedBy(radio);
        }
    }];
}

- (void)initAnnotationTool {
    _annotationTool = [[PanoAnnotationTool alloc] initWithView:self toolOption:PanoAnnotationToolWhiteBoard];
    _annotationTool.delegate = self;
    [_annotationTool show];
}

- (void)initTopView {
    _topContentView = [[UIView alloc] init];
    _topContentView.backgroundColor = appMainColor();
    [self addSubview:_topContentView];
    [self bringSubviewToFront:_topContentView];
    
    [_topContentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.mas_equalTo(self);
    }];
    _backButton = [UIButton buttonWithType:UIButtonTypeCustom];
    
    [_backButton addTarget:self action:@selector(clickBack:) forControlEvents:UIControlEventTouchUpInside];
    [_topContentView addSubview:_backButton];
    
    _rightTopView = [[UIStackView alloc] init];
    _rightTopView.distribution = UIStackViewDistributionFillEqually;
    _rightTopView.spacing = 10;
    UIButton *undoBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [undoBtn setImage:[UIImage imageNamed:@"btn.whiteboard.undo"] forState:UIControlStateNormal];
    [undoBtn addTarget:self action:@selector(clickUndoButton:) forControlEvents:UIControlEventTouchUpInside];
    [undoBtn sizeToFit];
    [_rightTopView addArrangedSubview:undoBtn];
    
    UIButton *redoBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [redoBtn setImage:[UIImage imageNamed:@"btn.whiteboard.redo"] forState:UIControlStateNormal];
    [redoBtn addTarget:self action:@selector(clickRedoButton:) forControlEvents:UIControlEventTouchUpInside];
    [redoBtn sizeToFit];
    [_rightTopView addArrangedSubview:redoBtn];
    [_topContentView addSubview:_rightTopView];

    _topBarView = [[PanoWbTopView alloc] init];
    [_topContentView addSubview:_topBarView];
    
    [_backButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.topBarView);
        make.left.mas_equalTo(self.topContentView).mas_offset(LargeFixSpace);
        CGFloat top = pano_safeAreaInset(self).top + 6;
        make.top.mas_equalTo(self).mas_offset(top);
    }];

    [_rightTopView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.topBarView);
        make.width.mas_equalTo(80);
        make.right.mas_equalTo(self).mas_offset(-LargeFixSpace);
    }];

    [_topBarView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self.topContentView);
        make.bottom.mas_equalTo(self.topContentView).mas_offset(-7);
    }];
    [_topBarView.prevBtn addTarget:self action:@selector(clickPrevPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.nextBtn addTarget:self action:@selector(clickNextPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.addBtn addTarget:self action:@selector(clickAddPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.deleteBtn addTarget:self action:@selector(clickRemovePageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.moreButton addTarget:self action:@selector(toggleMoreAction:) forControlEvents:UIControlEventTouchUpInside];
    
    [self reloadTopBarView];
}

- (void)toggleMoreAction:(UIButton *)sender {
    if (!self.topBarView.isShowing) {
        [self showMenuView:true];
    } else {
        [self.topBarView hideMenuView];
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [_backButton mas_updateConstraints:^(MASConstraintMaker *make) {
        CGFloat top = pano_safeAreaInset(self).top + 6;
        NSLog(@"top: %f",top);
        make.top.mas_equalTo(self).mas_offset(top);
    }];
}

- (void)showMenuView:(BOOL)animated {
    [self.annotationTool.penView dismiss];
    __weak typeof(self) weakSelf = self;
    PanoWhiteboardService *wbService = PanoCallClient.shared.wb;
    NSArray *items = nil;
    if ([wbService isPresenter]) {
        NSMutableArray *files = [NSMutableArray array];
        NSString *curFileName = [wbService getCurrentFileName];
        UIColor *color = [UIColor pano_colorWithHexString:@"#666666"];
        CGFloat size = 24;
        [[wbService fileNames] enumerateObjectsUsingBlock:^(NSString * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            UIImage *image = [curFileName isEqualToString:obj] ? IconFontImage(size, @"\U0000e78f", appHighlightedColor()) : nil;
            PanoItem *fileItem = [[PanoItem alloc] initWithImage:image title:obj configBlock:^{
                [wbService switchToDoc:obj];
            }];
            [files addObject:fileItem];
        }];
        PanoItem *prePageItem = [[PanoItem alloc] initWithImage:IconFontImage(size, @"\U0000e785", color) title:NSLocalizedString(@"Prev Page", nil) configBlock:^{
            [weakSelf clickPrevPageButton:nil];
        }];
        PanoItem *nextPageItem = [[PanoItem alloc] initWithImage:IconFontImage(size, @"\U0000e784", color) title:NSLocalizedString(@"Next Page", nil) configBlock:^{
            [weakSelf clickNextPageButton:nil];
        }];
        PanoItem *addPageItem = [[PanoItem alloc] initWithImage:IconFontImage(size, @"\U0000e789", color) title:NSLocalizedString(@"Add Page", nil) configBlock:^{
            [weakSelf clickAddPageButton:nil];
        }];
        PanoItem *removePageItem = [[PanoItem alloc] initWithImage:IconFontImage(size, @"\U0000e786", color) title:NSLocalizedString(@"Delete Page", nil) configBlock:^{
            [weakSelf clickRemovePageButton:nil];
        }];
        items = @[@[prePageItem, nextPageItem, addPageItem, removePageItem], [files copy]];
    } else {
//        PanoItem *applyItem = [[PanoItem alloc] initWithImage:[UIImage imageNamed:@"btn.whiteboard.apply"] title:NSLocalizedString(@"Apply Show", nil) configBlock:^{
//            [wbService applyBecomePresenter];
//        }];
        items = @[]; //@[@[applyItem]];
    }
    _moreItems = [[PanoArrayDataSource alloc] initWithItems:items cellIdentifier:@"cellID" configureCellBlock:^(PanoMenuCell*  _Nonnull cell, PanoItem*  _Nonnull object) {
        cell.descLabel.text = object.title;
        cell.icon.image = object.image;
        cell.contentView.backgroundColor = [UIColor whiteColor];
    }];
    [_topBarView.topMoreView.tableView registerClass:[PanoMenuCell class] forCellReuseIdentifier:@"cellID"];
    _topBarView.topMoreView.fileName = [wbService getCurrentFileName];
    _topBarView.topMoreView.presnterName = [PanoCallClient.shared.userMgr host].userName;
    _topBarView.topMoreView.tableView.dataSource = _moreItems;
    [self.topBarView showMenuViewInView:self animated:animated selectedBlock:^(UITableView * _Nonnull tableView, NSIndexPath * _Nonnull indexPath) {
        [weakSelf.topBarView hideMenuView];
        PanoArrayDataSource *dataSource = tableView.dataSource;
        id<PanoItemDelegate> item = [[dataSource.items objectAtIndex:indexPath.section] objectAtIndex:indexPath.row];
        if (item.clickBlock) {
            item.clickBlock();
        };
    }];
}

- (void)dismiss {
    [self clickBack:nil];
}

- (void)clickBack:(id)sender {
    if (PanoCallClient.shared.userMgr.isHost) {
        [self closeWhiteboard];
    } else {
        [self.delegate onMyRoleBecomeViewer];
    }
}

- (void)clickUndoButton:(id)sender {
    [self.whiteboardEngine undo];
}

- (void)clickRedoButton:(id)sender {
    [self.whiteboardEngine redo];
}

- (void)clickPrevPageButton:(id)sender {
    [self.whiteboardEngine prevPage];
}

- (void)clickNextPageButton:(id)sender {
    [self.whiteboardEngine nextPage];
}

- (void)clickAddPageButton:(id)sender {
    [self.whiteboardEngine addPage:YES];
}

- (void)clickRemovePageButton:(id)sender {
    [self.whiteboardEngine removePage:self.curPage];
}

#pragma mark - PanoRtcWhiteboardDelegate

- (void)onPageNumberChanged:(PanoWBPageNumber)curPage
             withTotalPages:(UInt32)totalPages {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updatePageNumber:curPage totalPages:totalPages];
    });
}

- (void)onViewScaleChanged:(Float32)scale {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateZoomScale:scale];
    });
}

- (void)onRoleTypeChanged:(PanoWBRoleType)newRole {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoCallClient.shared.config.wbRole = newRole;
    });
}

#pragma mark - Private
- (void)initWhiteboard {
    [self.whiteboardEngine setRoleType:self.config.wbRole];
    [self.whiteboardEngine setToolType:self.config.wbToolType];
    [self updateAnnotationConfig];
    [self updatePageNumber:[self.whiteboardEngine getCurrentPageNumber] totalPages:[self.whiteboardEngine getTotalNumberOfPages]];
    [self updateZoomScale:[self.whiteboardEngine getCurrentScaleFactor]];
}

- (void)openWhiteboard {
    [PanoCallClient.shared.wb addDelegate:self];
    [self.whiteboardEngine initVision:self.visionConfig];
    [self.whiteboardEngine open:self.drawView];
    [self.whiteboardEngine setOption:@(true) forType:kPanoWBOptionCursorPosSync];
    [self.whiteboardEngine setOption:@(true) forType:kPanoWBOptionShowRemoteCursor];
}

- (void)closeWhiteboard {
    [PanoCallClient.shared.wb close];
}

- (void)updatePageNumber:(PanoWBPageNumber)curPage totalPages:(UInt32)totalPages {
    self.curPage = curPage;
    self.totalPages = totalPages;
    self.topBarView.pageNumber.text = [NSString stringWithFormat:@"%u/%u", (unsigned int)curPage, (unsigned int)totalPages];
}

- (void)updateZoomScale:(Float32)scale {
    self.topBarView.zoomScale.text = [NSString stringWithFormat:@"%d%%", (int)(scale * 100)];
}

- (void)updateWhiteboardTool:(PanoWBToolType)toolType {
    if (PanoCallClient.shared.config.wbToolType != toolType) {
        PanoCallClient.shared.config.wbToolType = toolType;
        [self.whiteboardEngine setToolType:toolType];
    }
}

- (void)setButtonRoundedCorners:(UIButton *)button withRadius:(CGFloat)radius {
    button.layer.cornerRadius = radius;
    button.layer.masksToBounds = YES;
}

- (PanoWBColor *)convertWBColor:(UIColor *)color {
    CGFloat red = 0.0, green = 0.0, blue = 0.0, alpha = 0.0;
    [color getRed:&red green:&green blue:&blue alpha:&alpha];
    PanoWBColor * wbColor = [PanoWBColor new];
    wbColor.red = red;
    wbColor.green = green;
    wbColor.blue = blue;
    wbColor.alpha = alpha;
    return wbColor;
}

- (void)reloadTopBarView {
    if (self.topBarView.isShowing) {
        [self showMenuView:true];
    }
    [_backButton setTitle:NSLocalizedString(PanoCallClient.shared.userMgr.isHost ? @"关闭白板" : @"收起工具栏", nil) forState:UIControlStateNormal];
    _topBarView.moreButton.hidden = ![PanoCallClient.shared.userMgr isHost];
}

#pragma mark -- PanoWhiteboardDelegate
- (void)onPresenterDidChanged {
    [self reloadTopBarView];
}

- (void)onDocFilesDidChanged {
    [self reloadTopBarView];
}

#pragma mark -- PanoAnnotationToolDelegate
- (void)annotationToolDidChooseType:(PanoWBToolType)type options:(NSDictionary<PanoAnnotationToolKey,id> *)options {
    if (options[PanoToolKeyColor] != NULL) {
        self.config.wbColor = options[PanoToolKeyColor];
    }
    if (options[PanoToolKeyLineWidth] != NULL) {
        self.config.wbLineWidth = [options[PanoToolKeyLineWidth] unsignedIntValue];
    }
    if (options[PanoToolKeyFill] != NULL) {
        self.config.fillType = [options[PanoToolKeyFill] boolValue] ? kPanoWBFillColor : kPanoWBFillNone;
    }
    if (options[PanoToolKeyFontSize] != NULL) {
        self.config.wbFontSize = [options[PanoToolKeyFontSize] unsignedIntValue];
    }
    NSLog(@"wbLineWidth: %u",(unsigned int)PanoCallClient.shared.config.wbLineWidth);
    [self updateAnnotationConfig];
    [self.whiteboardEngine setToolType:type];
}

- (void)updateAnnotationConfig {
    PanoWBColor *wbColor = [self convertWBColor:self.config.wbColor];
    [self.whiteboardEngine setForegroundColor:wbColor];
    [self.whiteboardEngine setFillType:self.config.fillType];
    [self.whiteboardEngine setLineWidth:self.config.wbLineWidth];
    [self.whiteboardEngine setFontSize:self.config.wbFontSize];
    if (self.config.fillType == kPanoWBFillColor) {
        [self.whiteboardEngine setFillColor:wbColor];
    }
}

- (void)annotationToolDidChoosed:(PanoAnnotationToolOption)option {
    switch (option) {
        case PanoAnnotationToolUndo:
            [self clickUndoButton:nil];
            break;
        case PanoAnnotationToolRedo:
            [self clickRedoButton:nil];
            break;
        case PanoAnnotationToolClose:
            [self clickBack:nil];
            break;
        default:
            break;
    }
}

- (void)setEnable:(BOOL)enable {
    _enable = enable;
    if (enable) {
        self.topContentView.hidden = false;
        [self.annotationTool show];
        [self.whiteboardEngine setRoleType:kPanoWBRoleAttendee];
    } else {
        [self.annotationTool hide];
        self.topContentView.hidden = true;
        [self.whiteboardEngine setRoleType:kPanoWBRoleViewer];
    }
}

- (void)dealloc {
    [PanoCallClient.shared.wb removeDelegate:self];
}

@end
