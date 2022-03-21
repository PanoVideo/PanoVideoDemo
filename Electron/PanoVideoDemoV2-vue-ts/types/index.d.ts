declare interface UserInfo {
  lastActiveTime: number;
  userId: string;
  userName: string;
  videoDomRef: HTMLElement;
  videoMuted: boolean;
  audioMuted: boolean;
  screenOpen: boolean;
  screenShareType: 'screen' | 'application' | 'none';
  shareAnnotationOn: boolean;
  videoAnnotationOn: boolean;
  externalAnnotationOn: boolean;
  screenDomRef: HTMLElement;
}

declare interface MeetingStatus {
  meetingStatus:
    | 'notstart'
    | 'disconnected'
    | 'connected'
    | 'reconnecting'
    | 'countdownover'
    | 'ended' = 'notstart';
}
