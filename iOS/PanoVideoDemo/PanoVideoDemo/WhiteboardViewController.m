//
//  WhiteboardViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "WhiteboardViewController.h"
#import "PanoCallClient.h"
#import "StylesView.h"

@interface WhiteboardViewController () <PanoRtcWhiteboardDelegate>

@property (strong, nonatomic) IBOutlet UIButton * selectButton;
@property (strong, nonatomic) IBOutlet UIButton * pathButton;
@property (strong, nonatomic) IBOutlet UIButton * shapesButton;
@property (strong, nonatomic) IBOutlet UIButton * textButton;
@property (strong, nonatomic) IBOutlet UIButton * stylesButton;
@property (strong, nonatomic) IBOutlet UIButton * eraserButton;
@property (strong, nonatomic) IBOutlet UIView * shapesView;
@property (strong, nonatomic) IBOutlet UIButton * lineButton;
@property (strong, nonatomic) IBOutlet UIButton * ellipseButton;
@property (strong, nonatomic) IBOutlet UIButton * rectButton;
@property (strong, nonatomic) IBOutlet StylesView * stylesView;
@property (strong, nonatomic) IBOutlet UIView * drawView;
@property (strong, nonatomic) IBOutlet UILabel * pageNumber;
@property (strong, nonatomic) IBOutlet UILabel * zoomScale;

@property (weak, nonatomic) PanoRtcWhiteboard * whiteboardEngine;
@property (assign, nonatomic) PanoWBPageNumber curPage;
@property (assign, nonatomic) UInt32 totalPages;

@end

@implementation WhiteboardViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self initVideoView];
    self.whiteboardEngine = PanoCallClient.sharedInstance.engineKit.whiteboardEngine;
    [self initWhiteboard];
    [self openWhiteboard];
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewDidOpen)]) {
        [self.whiteboardViewDelegate whiteboardViewDidOpen];
    }
}

- (IBAction)clickBack:(id)sender {
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewWillClose)]) {
        [self.whiteboardViewDelegate whiteboardViewWillClose];
    }
    [self closeWhiteboard];
    [self dismissViewControllerAnimated:YES completion:nil];
    if ([self.whiteboardViewDelegate respondsToSelector:@selector(whiteboardViewDidClose)]) {
        [self.whiteboardViewDelegate whiteboardViewDidClose];
    }
}

- (IBAction)clickSelectButton:(id)sender {
    if (!self.selectButton.selected) {
        [self resetAllButtons];
        [self updateSelectButton:YES];
        [self updateWhiteboardTool:kPanoWBToolSelect];
    }
}

- (IBAction)clickPathButton:(id)sender {
    if (!self.pathButton.selected) {
        [self resetAllButtons];
        [self updatePathButton:YES];
        [self updateWhiteboardTool:kPanoWBToolPath];
    }
}

- (IBAction)clickShapesButton:(id)sender {
    [self updateShapesButton:!self.shapesButton.selected];
    if (self.shapesButton.selected) {
        [self updateStylesButton:NO];
    }
}

- (IBAction)clickTextButton:(id)sender {
    if (!self.textButton.selected) {
        [self resetAllButtons];
        [self updateTextButton:YES];
        [self updateWhiteboardTool:kPanoWBToolText];
    }
}

- (IBAction)clickStylesButton:(id)sender {
    [self updateStylesButton:!self.stylesButton.selected];
    if (self.stylesButton.selected) {
        [self updateShapesButton:NO];
    }
}

- (IBAction)clickEraserButton:(id)sender {
    if (!self.eraserButton.selected) {
        [self resetAllButtons];
        [self updateEraserButton:YES];
        [self updateWhiteboardTool:kPanoWBToolEraser];
    }
}

- (IBAction)clickUndoButton:(id)sender {
    [self.whiteboardEngine undo];
}

- (IBAction)clickRedoButton:(id)sender {
    [self.whiteboardEngine redo];
}

- (IBAction)clickLineButton:(id)sender {
    if (!self.lineButton.selected) {
        [self resetAllButtons];
        [self updateLineButton:YES];
        [self updateShapesButton:NO];
        [self updateWhiteboardTool:kPanoWBToolLine];
    }
}

- (IBAction)clickEllipseButton:(id)sender {
    if (!self.ellipseButton.selected) {
        [self resetAllButtons];
        [self updateEllipseButton:YES];
        [self updateShapesButton:NO];
        [self updateWhiteboardTool:kPanoWBToolEllipse];
    }
}

- (IBAction)clickRectButton:(id)sender {
    if (!self.rectButton.selected) {
        [self resetAllButtons];
        [self updateRectButton:YES];
        [self updateShapesButton:NO];
        [self updateWhiteboardTool:kPanoWBToolRect];
    }
}

- (IBAction)clickColorButton:(UIButton *)button {
    [self.stylesView selectColorButton:(ColorButton *)button];
    [self updateStylesButton:NO];
    PanoCallClient.sharedInstance.wbColor = button.backgroundColor;
    [self.whiteboardEngine setForegroundColor:[self convertWBColor:button.backgroundColor]];
}

- (IBAction)clickPrevPageButton:(id)sender {
    [self.whiteboardEngine prevPage];
}

- (IBAction)clickNextPageButton:(id)sender {
    [self.whiteboardEngine nextPage];
}

- (IBAction)clickAddPageButton:(id)sender {
    [self.whiteboardEngine addPage:YES];
}

- (IBAction)clickRemovePageButton:(id)sender {
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
        PanoCallClient.sharedInstance.wbRole = newRole;
    });
}


#pragma mark - Private

- (void)initVideoView {
    self.user1View.hidden = YES;
    self.user2View.hidden = YES;
    self.user3View.hidden = YES;
    self.user4View.hidden = YES;
}

- (void)initWhiteboard {
    [self.whiteboardEngine setRoleType:PanoCallClient.sharedInstance.wbRole];
    [self.whiteboardEngine setToolType:PanoCallClient.sharedInstance.wbToolType];
    [self.whiteboardEngine setLineWidth:PanoCallClient.sharedInstance.wbLineWidth];
    [self.whiteboardEngine setForegroundColor:[self convertWBColor:PanoCallClient.sharedInstance.wbColor]];
    [self.whiteboardEngine setFontStyle:PanoCallClient.sharedInstance.wbFontStyle];
    [self.whiteboardEngine setFontSize:PanoCallClient.sharedInstance.wbFontSize];
    
    [self initSelectedButton];
    [self initStylesButton];
    [self updatePageNumber:[self.whiteboardEngine getCurrentPageNumber] totalPages:[self.whiteboardEngine getTotalNumberOfPages]];
    [self updateZoomScale:[self.whiteboardEngine getCurrentScaleFactor]];
}

- (void)openWhiteboard {
    [self.whiteboardEngine setDelegate:self];
    [self.whiteboardEngine open:self.drawView];
}

- (void)closeWhiteboard {
    [self.whiteboardEngine close];
    [self.whiteboardEngine setDelegate:nil];
}

- (void)resetAllButtons {
    [self updateSelectButton:NO];
    [self updatePathButton:NO];
    [self updateLineButton:NO];
    [self updateEllipseButton:NO];
    [self updateRectButton:NO];
    [self updateTextButton:NO];
    [self updateEraserButton:NO];
    [self updateShapesButton:NO];
    [self updateStylesButton:NO];
}

- (void)initSelectedButton {
    [self resetAllButtons];
    switch (PanoCallClient.sharedInstance.wbToolType) {
        case kPanoWBToolSelect:
            [self updateSelectButton:YES];
            break;
        case kPanoWBToolPath:
            [self updatePathButton:YES];
            break;
        case kPanoWBToolLine:
            [self updateLineButton:YES];
            [self updateShapesButton:NO];
            break;
        case kPanoWBToolRect:
            [self updateRectButton:YES];
            [self updateShapesButton:NO];
            break;
        case kPanoWBToolEllipse:
            [self updateEllipseButton:YES];
            [self updateShapesButton:NO];
            break;
        case kPanoWBToolText:
            [self updateTextButton:YES];
            break;
        case kPanoWBToolEraser:
            [self updateEraserButton:YES];
            break;
        default:
            break;
    }
}

- (void)initStylesButton {
    [self.stylesView selectColor:PanoCallClient.sharedInstance.wbColor];
}

- (void)updateSelectButton:(BOOL)selected {
    self.selectButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.select" : @"btn.whiteboard.select-selected")];
    [self.selectButton setImage:image forState:UIControlStateNormal];
}

- (void)updatePathButton:(BOOL)selected {
    self.pathButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.path" : @"btn.whiteboard.path-selected")];
    [self.pathButton setImage:image forState:UIControlStateNormal];
}

- (void)updateShapesButton:(BOOL)selected {
    self.shapesButton.selected = selected;
    BOOL imageSelected = selected || self.lineButton.selected || self.ellipseButton.selected || self.rectButton.selected;
    UIImage * image = [UIImage imageNamed:(!imageSelected ? @"btn.whiteboard.shapes" : @"btn.whiteboard.shapes-selected")];
    [self.shapesButton setImage:image forState:UIControlStateNormal];
    self.shapesView.hidden = !selected;
}

- (void)updateTextButton:(BOOL)selected {
    self.textButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.text" : @"btn.whiteboard.text-selected")];
    [self.textButton setImage:image forState:UIControlStateNormal];
}

- (void)updateStylesButton:(BOOL)selected {
    self.stylesButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.styles" : @"btn.whiteboard.styles-selected")];
    [self.stylesButton setImage:image forState:UIControlStateNormal];
    self.stylesView.hidden = !selected;
}

- (void)updateEraserButton:(BOOL)selected {
    self.eraserButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.eraser" : @"btn.whiteboard.eraser-selected")];
    [self.eraserButton setImage:image forState:UIControlStateNormal];
}

- (void)updateLineButton:(BOOL)selected {
    self.lineButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.line" : @"btn.whiteboard.line-selected")];
    [self.lineButton setImage:image forState:UIControlStateNormal];
}

- (void)updateEllipseButton:(BOOL)selected {
    self.ellipseButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.ellipse" : @"btn.whiteboard.ellipse-selected")];
    [self.ellipseButton setImage:image forState:UIControlStateNormal];
}

- (void)updateRectButton:(BOOL)selected {
    self.rectButton.selected = selected;
    UIImage * image = [UIImage imageNamed:(!selected ? @"btn.whiteboard.rect" : @"btn.whiteboard.rect-selected")];
    [self.rectButton setImage:image forState:UIControlStateNormal];
}

- (void)updatePageNumber:(PanoWBPageNumber)curPage totalPages:(UInt32)totalPages {
    self.curPage = curPage;
    self.totalPages = totalPages;
    self.pageNumber.text = [NSString stringWithFormat:@"%u/%u", (unsigned int)curPage, (unsigned int)totalPages];
}

- (void)updateZoomScale:(Float32)scale {
    self.zoomScale.text = [NSString stringWithFormat:@"%d%%", (int)(scale * 100)];
}

- (void)updateWhiteboardTool:(PanoWBToolType)toolType {
    if (PanoCallClient.sharedInstance.wbToolType != toolType) {
        PanoCallClient.sharedInstance.wbToolType = toolType;
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

@end
