import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import QRCode from 'qrcode';

type InviteQrPreviewPayload = {
  joinCode: string;
  joinUrl: string;
};

@Component({
  selector: 'app-instructor-invite-qr',
  templateUrl: './instructor-invite-qr.html',
  styleUrl: './instructor-invite-qr.css',
})
export class InstructorInviteQr implements OnInit {
  private readonly route = inject(ActivatedRoute);

  protected readonly qrCodeDataUrl = signal('');
  protected readonly joinCode = signal('');
  protected readonly errorMessage = signal('');

  async ngOnInit() {
    await this.loadInvitePreview();
  }

  private async loadInvitePreview() {
    const inviteId = this.route.snapshot.paramMap.get('inviteId')?.trim() ?? '';
    if (!inviteId) {
      this.errorMessage.set('Invite preview unavailable. Generate a new invite from the lecture page.');
      return;
    }

    const payload = this.readInvitePayload();
    if (!payload) {
      this.errorMessage.set('Invite preview expired. Generate a new invite from the lecture page.');
      return;
    }

    this.joinCode.set(payload.joinCode.trim() || inviteId);

    try {
      const qrCodeDataUrl = await QRCode.toDataURL(payload.joinUrl, {
        width: 640,
        margin: 1,
        errorCorrectionLevel: 'M',
      });
      this.qrCodeDataUrl.set(qrCodeDataUrl);
      this.errorMessage.set('');
    } catch {
      this.errorMessage.set('Could not generate QR code. Generate a new invite from the lecture page.');
    }
  }

  private readInvitePayload(): InviteQrPreviewPayload | null {
    const rawHashPayload = window.location.hash.startsWith('#') ? window.location.hash.slice(1) : '';
    if (!rawHashPayload) {
      return null;
    }

    this.clearPreviewHash();

    try {
      const parsedPayload = JSON.parse(decodeURIComponent(rawHashPayload)) as Partial<InviteQrPreviewPayload>;
      const joinUrl = parsedPayload.joinUrl?.trim() ?? '';
      const joinCode = parsedPayload.joinCode?.trim() ?? '';
      if (!joinUrl) {
        return null;
      }
      return {
        joinCode,
        joinUrl,
      };
    } catch {
      return null;
    }
  }

  private clearPreviewHash() {
    window.history.replaceState(null, '', window.location.pathname + window.location.search);
  }
}
