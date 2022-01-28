import { RtcMessage } from '@pano.video/panortc';
import { PROP_HOST_ID } from '@/vars';

export function setHostId(hostId) {
  RtcMessage.getInstance().setProperty(PROP_HOST_ID, {
    id: hostId,
  });
}
