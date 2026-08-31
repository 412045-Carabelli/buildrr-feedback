import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface ItemMenu {
  label: string;
  path: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() visible = true;

  menuItems: ItemMenu[] = [
    { label: 'Tickets', path: '/tickets', icon: 'pi-list' },
    { label: 'Nuevo ticket', path: '/tickets/nuevo', icon: 'pi-plus-circle' }
  ];
}
