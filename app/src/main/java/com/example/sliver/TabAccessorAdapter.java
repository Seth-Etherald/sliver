package com.example.sliver;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessorAdapter extends FragmentPagerAdapter {
  public TabAccessorAdapter(@NonNull FragmentManager fm) {
    super(fm);
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return new ChatsFragment();
      case 1:
        return new GroupsFragment();
      case 2:
        return new ContactsFragment();
      case 3:
        return new RequestsFragment();
      default:
        return null;
    }
  }

  @Override
  public int getCount() {
    return 4;
  }

  @NonNull
  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
      case 0:
        SpannableString Chats = new SpannableString("Chats");
        Chats.setSpan(new AbsoluteSizeSpan(13, true), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return Chats;

      case 1:
        SpannableString Groups = new SpannableString("Groups");
        Groups.setSpan(new AbsoluteSizeSpan(13, true), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return Groups;

      case 2:
        SpannableString Contacts = new SpannableString("Contacts");
        Contacts.setSpan(new AbsoluteSizeSpan(13, true), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return Contacts;

      case 3:
        SpannableString Requests = new SpannableString("Requests");
        Requests.setSpan(new AbsoluteSizeSpan(13, true), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return Requests;
      default:
        return null;
    }
  }
}
