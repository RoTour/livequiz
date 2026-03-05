import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CreateInviteResponse, LectureInviteResponse } from '../../../lecture.service';
import { HumanDatePipe } from '../../../shared/date/human-date.pipe';

@Component({
  selector: 'app-invite-management-panel',
  imports: [HumanDatePipe],
  templateUrl: './invite-management-panel.html',
  styleUrl: './invite-management-panel.css',
})
export class InviteManagementPanel {
  @Input({ required: true }) selectedLectureId!: string;
  @Input({ required: true })
  set lastCreatedInvite(value: CreateInviteResponse | null) {
    this._lastCreatedInvite = value;
    this.copyStatus = '';
    this.showJoinLink = false;
  }

  get lastCreatedInvite(): CreateInviteResponse | null {
    return this._lastCreatedInvite;
  }

  @Input({ required: true }) qrCodeDataUrl!: string;
  @Input({ required: true })
  set qrCodeError(value: string) {
    this._qrCodeError = value;
    if (value) {
      this.showJoinLink = true;
    }
  }

  get qrCodeError(): string {
    return this._qrCodeError;
  }

  @Input({ required: true }) invites!: LectureInviteResponse[];
  @Output() createInvite = new EventEmitter<void>();
  @Output() refreshInvites = new EventEmitter<void>();
  @Output() revokeInvite = new EventEmitter<string>();

  protected copyStatus = '';
  protected showJoinLink = false;

  private _lastCreatedInvite: CreateInviteResponse | null = null;
  private _qrCodeError = '';

  protected create() {
    this.createInvite.emit();
  }

  protected refresh() {
    this.refreshInvites.emit();
  }

  protected revoke(inviteId: string) {
    this.revokeInvite.emit(inviteId);
  }

  protected async copyJoinLink(joinUrl: string) {
    const normalizedJoinUrl = joinUrl.trim();
    if (!normalizedJoinUrl) {
      this.copyStatus = 'Invite link unavailable.';
      return;
    }

    const clipboardApi = navigator.clipboard;
    if (!clipboardApi?.writeText) {
      this.copyStatus = 'Clipboard unavailable. Use "Show link" to share manually.';
      return;
    }

    try {
      await clipboardApi.writeText(normalizedJoinUrl);
      this.copyStatus = 'Join link copied.';
    } catch {
      this.copyStatus = 'Could not copy link. Use "Show link" to share manually.';
    }
  }

  protected toggleJoinLinkVisibility() {
    this.showJoinLink = !this.showJoinLink;
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
