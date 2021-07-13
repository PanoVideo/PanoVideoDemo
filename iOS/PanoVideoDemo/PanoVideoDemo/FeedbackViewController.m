//
//  FeedbackViewController.m
//  PanoVideoDemo
//  Copyright © 2020 Pano. All rights reserved.
//

#import "FeedbackViewController.h"
#import "PanoCallClient.h"

static NSUInteger kMaxDescriptionCharacters = 300;

static NSTimeInterval kDelayDismissAlertTime = 3.0;

@interface FeedbackViewController () <UITextViewDelegate, UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UISegmentedControl * problemType;
@property (strong, nonatomic) IBOutlet UITextView * problemDescription;
@property (strong, nonatomic) IBOutlet UILabel * remainder;
@property (strong, nonatomic) IBOutlet UISwitch * uploadLog;
@property (strong, nonatomic) IBOutlet UITextField * contact;
@property (strong, nonatomic) IBOutlet UIButton * sendButton;

@property (strong, nonatomic) UIView * checkTextControl;

@end

@implementation FeedbackViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self initProblemDescription];
    [self initUploadLog];
    [self initContact];
    [self setupKeyboardEvents];
    [self setButtonRoundedCorners:self.sendButton withRadius:5.0];
}

- (void)dealloc {
    [self teardownKeyboardEvents];
}

- (IBAction)clickBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)clickSend:(id)sender {
    PanoFeedbackInfo * info = [PanoFeedbackInfo new];
    info.type = [self feedbackType];
    info.productName = PanoCallClient.productName;
    info.detailDescription = self.problemDescription.text;
    info.contact = self.contact.text;
    info.extraInfo = PanoCallClient.sharedInstance.uuid;
    info.uploadLogs = self.uploadLog.on;
    [PanoCallClient.sharedInstance.engineKit sendFeedback:info];
    [self dismissViewControllerAnimated:YES completion:nil];
//    [self presentAlert:NSLocalizedString(@"feedbackAlert", nil)];
}

#pragma mark - UITextViewDelegate

- (void)textViewDidChange:(UITextView *)textView {
    NSUInteger inputedCharacters = textView.text.length;
    if (inputedCharacters > kMaxDescriptionCharacters) {
        textView.text = [textView.text substringToIndex:kMaxDescriptionCharacters];
        [textView.undoManager removeAllActions];
        [textView becomeFirstResponder];
        inputedCharacters = kMaxDescriptionCharacters;
    }
    NSUInteger remaining = kMaxDescriptionCharacters - inputedCharacters;
    self.remainder.text = [NSString stringWithFormat:NSLocalizedString(@"remainder", nil), remaining];
}

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView {
    self.checkTextControl = textView;
    return YES;
}

- (void)textViewDidEndEditing:(UITextView *)textView {
    self.checkTextControl = nil;
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    self.checkTextControl = textField;
    return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    self.checkTextControl = nil;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

# pragma mark Keyboard Events

- (void)setupKeyboardEvents {
    // Add observer for keyboard events
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:)name:UIKeyboardWillChangeFrameNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

- (void)teardownKeyboardEvents {
    // Remove observer for keyboard events
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)keyboardWillShow:(NSNotification*)notification {
    if (nil == self.checkTextControl) {
        return;
    }
    
    /*
     Reduce the size of the text view so that it's not obscured by thekeyboard.
     Animate the resize so that it's in sync with the appearance of thekeyboard.
     */
    NSDictionary *userInfo = [notification userInfo];
    // Get the origin of the keyboard when it's displayed.
    NSValue* aValue = [userInfo objectForKey:UIKeyboardFrameEndUserInfoKey];
    // Get the top of the keyboard as the y coordinate of its origin inself's view's coordinate system. The bottom of the text view's frame shouldalign with the top of the keyboard's final position.
    CGRect keyboardRect = [aValue CGRectValue];
    
    // Get the duration of the animation.
    NSValue *animationDurationValue = [userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval animationDuration;
    [animationDurationValue getValue:&animationDuration];
    
    // Get the move height
    CGRect textFrame = [self.checkTextControl convertRect:self.checkTextControl.bounds toView:self.view];
    float textY = textFrame.origin.y + textFrame.size.height;
    float bottomY = self.view.frame.size.height - textY;
    if (bottomY >= keyboardRect.size.height) {
        return;
    }
    float moveY = keyboardRect.size.height - bottomY;
    
    // Animate the resize of the text view's frame in sync with the keyboard'sappearance.
    [self moveInputBarWithKeyboardHeight:moveY withDuration:animationDuration];
}

- (void)keyboardWillHide:(NSNotification*)notification {
    /*
     Restore the size of the text view (fill self's view).
     Animate the resize so that it's in sync with the disappearance of thekeyboard.
     */
    NSDictionary* userInfo = [notification userInfo];
    NSValue *animationDurationValue = [userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval animationDuration;
    [animationDurationValue getValue:&animationDuration];
    [self moveInputBarWithKeyboardHeight:0.0 withDuration:animationDuration];
}

- (void)moveInputBarWithKeyboardHeight:(float)height withDuration:(NSTimeInterval)duration {
    CGRect rect = self.view.frame;
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:duration];
    rect.origin.y = -height;
    self.view.frame = rect;
    [UIView commitAnimations];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark - Alert

- (void)presentAlert:(NSString *)message {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle: UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil) style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
    [self performSelector:@selector(dismissAlert:) withObject:alert afterDelay:kDelayDismissAlertTime];
}

- (void)dismissAlert:(UIAlertController *)alert{
    [alert dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Private

- (void)initProblemDescription {
    self.problemDescription.delegate = self;
    self.problemDescription.text = @"";
    self.problemDescription.layer.borderWidth = 1.0;
    self.problemDescription.layer.cornerRadius = 5.0;
    self.problemDescription.layer.borderColor = [UIColor.grayColor colorWithAlphaComponent:0.3].CGColor;
    self.remainder.text = [NSString stringWithFormat:NSLocalizedString(@"remainder", nil), kMaxDescriptionCharacters];
}

- (void)initUploadLog {
    self.uploadLog.on = YES;
}

- (void)initContact {
    self.contact.delegate = self;
    self.contact.text = @"";
}

- (PanoFeedbackType)feedbackType {
    PanoFeedbackType type = kPanoFeedbackGeneral;
    switch (self.problemType.selectedSegmentIndex) {
        case 0:
            type = kPanoFeedbackAudio;
            break;
        case 1:
            type = kPanoFeedbackVideo;
            break;
        case 2:
            type = kPanoFeedbackWhiteboard;
            break;
        default:
            type = kPanoFeedbackGeneral;
            break;
    }
    return type;
}

- (void)setButtonRoundedCorners:(UIButton *)button withRadius:(CGFloat)radius {
    button.layer.cornerRadius = radius;
    button.layer.masksToBounds = YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        return UIInterfaceOrientationMaskPortrait;
    }
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

@end
