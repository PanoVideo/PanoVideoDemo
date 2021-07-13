//
//  PanoFeedAudioController.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoFeedAudioController.h"
#import "PanoCallClient.h"

@interface PanoFeedAudioController () <UITextViewDelegate>
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UIButton *sendButton;

@end

@implementation PanoFeedAudioController

- (void)viewDidLoad {
    [super viewDidLoad];
    _textView.layer.cornerRadius = 5;
    _textView.layer.masksToBounds = true;
    _textView.layer.borderWidth = 1;
    _textView.layer.borderColor = [[UIColor lightGrayColor] CGColor];
    _textView.delegate = self;
    [self.view addGestureRecognizer:[[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(hideKeyboard)]];
}

- (void)hideKeyboard {
    [_textView resignFirstResponder];
}

- (void)textViewDidChange:(UITextView *)textView {
    _sendButton.enabled =  textView.text.length > 0;
}

- (IBAction)sendLogAction:(UIButton *)sender {
    NSDictionary *startDumpMsg = @{ @"type" : @"command", @"command" : @"startDump", @"description": _textView.text ?: @""};
    [PanoCallClient.sharedInstance.rtmService broadcastMessage:startDumpMsg sendBack:true];
    [self dismiss:nil];
    
}
- (IBAction)dismiss:(id)sender {
    [self dismissViewControllerAnimated:true completion:nil];
}

@end
