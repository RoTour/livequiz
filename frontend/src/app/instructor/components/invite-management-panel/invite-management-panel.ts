import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CreateInviteResponse, LectureInviteResponse } from '../../../lecture.service';

@Component({
  selector: 'app-invite-management-panel',
  imports: [],
  templateUrl: './invite-management-panel.html',
})
export class InviteManagementPanel {
  @Input({ required: true }) selectedLectureId!: string;
  @Input({ required: true }) lastCreatedInvite!: CreateInviteResponse | null;
  @Input({ required: true }) invites!: LectureInviteResponse[];
  @Output() createInvite = new EventEmitter<void>();
  @Output() refreshInvites = new EventEmitter<void>();
  @Output() revokeInvite = new EventEmitter<string>();

  protected create() {
    this.createInvite.emit();
  }

  protected refresh() {
    this.refreshInvites.emit();
  }

  protected revoke(inviteId: string) {
    this.revokeInvite.emit(inviteId);
  }

  protected inviteDisplayStatus(invite: LectureInviteResponse): string {
    if (invite.active) {
      return 'active';
    }
    if (invite.revokedAt) {
      return 'revoked';
    }
    return 'expired';
  }
}
