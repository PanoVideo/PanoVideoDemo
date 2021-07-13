//
//  WhiteboardViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "WhiteboardViewController.h"
#import "PanoCallClient.h"
#import "PanoWbTopView.h"
#import "PanoWhiteboardPrivateHeader.h"
#import "PanoServiceManager.h"
#import "PanoArrayDataSource.h"
#import "PanoMenuCell.h"
#import "PanoAnnotationTool.h"
#import "UIImage+IconFont.h"

@interface WhiteboardViewController () <PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate,PanoAnnotationToolDelegate>

@property (strong, nonatomic) IBOutlet UIView * drawView;
@property (weak, nonatomic) PanoRtcWhiteboard * whiteboardEngine;
@property (assign, nonatomic) PanoWBPageNumber curPage;
@property (assign, nonatomic) UInt32 totalPages;
@property (strong, nonatomic) PanoWbTopView *topBarView;
@property (strong, nonatomic) PanoArrayDataSource *moreItems;
@property (strong, nonatomic) PanoAnnotationTool* annotationTool;
@end

@implementation WhiteboardViewController

- (PanoConfig *)config {
    return PanoCallClient.sharedInstance.config;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initTopBarView];
    self.whiteboardEngine = PanoCallClient.sharedInstance.engineKit.whiteboardEngine;
    [self initAnnotationTool];
    [self initWhiteboard];
    [self openWhiteboard];
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewDidOpen)]) {
        [self.whiteboardViewDelegate whiteboardViewDidOpen];
    }
}

- (void)initAnnotationTool {
    _annotationTool = [[PanoAnnotationTool alloc] initWithView:self.view toolOption:PanoAnnotationToolWhiteBoard];
    _annotationTool.delegate = self;
    _annotationTool.fontSizeRange = PanoMakeRange(1, 21);
    [_annotationTool show];
}

- (void)initTopBarView {
    _topBarView = [[PanoWbTopView alloc] init];
    [self.view addSubview:_topBarView];
    [self.view bringSubviewToFront:_topBarView];
    [_topBarView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.view).insets(UIEdgeInsetsMake(statusBarHeight() + DefaultFixSpace, 0, 0, 0));
        make.centerX.mas_equalTo(self.view);
    }];
    [_topBarView.prevBtn addTarget:self action:@selector(clickPrevPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.nextBtn addTarget:self action:@selector(clickNextPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.addBtn addTarget:self action:@selector(clickAddPageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.deleteBtn addTarget:self action:@selector(clickRemovePageButton:) forControlEvents:UIControlEventTouchUpInside];
    [_topBarView.moreButton addTarget:self action:@selector(toggleMoreAction:) forControlEvents:UIControlEventTouchUpInside];
}

- (void)toggleMoreAction:(UIButton *)sender {
    if (!self.topBarView.isShowing) {
        [self showMenuView:true];
    } else {
        [self.topBarView hideMenuView];
    }
}

- (void)showMenuView:(BOOL)animated {
    [self.annotationTool.penView dismiss];
    __weak typeof(self) weakSelf = self;
    PanoWhiteboardService *wbService = [PanoServiceManager serviceWithType:PanoWhiteboardServiceType];
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
        PanoItem *applyItem = [[PanoItem alloc] initWithImage:[UIImage imageNamed:@"btn.whiteboard.apply"] title:NSLocalizedString(@"Apply Show", nil) configBlock:^{
            [wbService applyBecomePresenter];
        }];
        items = @[@[applyItem]];
    }
    _moreItems = [[PanoArrayDataSource alloc] initWithItems:items cellIdentifier:@"cellID" configureCellBlock:^(PanoMenuCell*  _Nonnull cell, PanoItem*  _Nonnull object) {
        cell.descLabel.text = object.title;
        cell.icon.image = object.image;
        cell.contentView.backgroundColor = [UIColor whiteColor];
    }];
    [_topBarView.topMoreView.tableView registerClass:[PanoMenuCell class] forCellReuseIdentifier:@"cellID"];
    _topBarView.topMoreView.fileName = [wbService getCurrentFileName];
    _topBarView.topMoreView.presnterName = [wbService presenter].userName;
    _topBarView.topMoreView.tableView.dataSource = _moreItems;
    [self.topBarView showMenuViewInView:self.view animated:animated selectedBlock:^(UITableView * _Nonnull tableView, NSIndexPath * _Nonnull indexPath) {
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

- (void)dismissViewControllerAnimated:(BOOL)flag completion:(void (^)(void))completion {
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewWillClose)]) {
        [self.whiteboardViewDelegate whiteboardViewWillClose];
    }
    [super dismissViewControllerAnimated:flag completion:completion];
}

- (IBAction)clickBack:(id)sender {
    [self closeWhiteboard];
    [self dismissViewControllerAnimated:YES completion:nil];
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewDidClose)]) {
        [self.whiteboardViewDelegate whiteboardViewDidClose];
    }
}

- (IBAction)clickUndoButton:(id)sender {
    [self.whiteboardEngine undo];
}

- (IBAction)clickRedoButton:(id)sender {
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
        PanoCallClient.sharedInstance.config.wbRole = newRole;
    });
}


#pragma mark - Private
- (void)initWhiteboard {
    [self.whiteboardEngine setRoleType:self.config.wbRole];
    [self.whiteboardEngine setToolType:self.config.wbToolType];
    [self updateAnnotationConfig];
    // TODO
    [self updatePageNumber:[self.whiteboardEngine getCurrentPageNumber] totalPages:[self.whiteboardEngine getTotalNumberOfPages]];
    [self updateZoomScale:[self.whiteboardEngine getCurrentScaleFactor]];
}

- (void)openWhiteboard {
    PanoWhiteboardService *wbService = [PanoServiceManager serviceWithType:PanoWhiteboardServiceType];
    [wbService addDelegate:self];
    [self.whiteboardEngine open:self.drawView];
}

- (void)closeWhiteboard {
    [self.whiteboardEngine close];
    PanoWhiteboardService *wbService = [PanoServiceManager serviceWithType:PanoWhiteboardServiceType];
    [wbService removeDelegate:self];
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
    if (PanoCallClient.sharedInstance.config.wbToolType != toolType) {
        PanoCallClient.sharedInstance.config.wbToolType = toolType;
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

#pragma mark -- PanoWhiteboardDelegate

- (void)reloadTopBarView {
    if (self.topBarView.isShowing) {
        [self showMenuView:true];
    }
}

- (void)onPresenterDidChanged:(PanoUserInfo *)persenter {
    [self reloadTopBarView];
}

- (void)onDocFilesDidChanged {
    PanoWhiteboardService *wbService = [PanoServiceManager serviceWithType:PanoWhiteboardServiceType];
    NSMutableArray<NSString *> *files = [wbService fileNames];
    NSMutableArray<NSString *> *files2 = [wbService enumerateFiles];
    NSLog(@"%@ %@",files, files2);
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
    NSLog(@"wbLineWidth: %u",(unsigned int)PanoCallClient.sharedInstance.config.wbLineWidth);
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

@end
