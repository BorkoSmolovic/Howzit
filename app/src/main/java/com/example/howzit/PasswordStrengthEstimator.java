/*
This file is part of Howzit.
Copyright (C) 2020-2021 Mauka-Makai team: Nickolas Naydenov, Borko Smolovic, Iacopo Marri,
Viking Forsman, Matteo Cecini, Erblin Isaku, Alessandro Bertulli

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; version 2.1.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, see <https://www.gnu.org/licenses>.

Linking Howzit statically or dynamically with other modules is making a combined work
based on Howzit. Thus, the terms and conditions of the GNU Lesser General Public License
cover the whole combination.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of AndroidX under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Material Components for Android under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Lottie under the Apache v.2.0 License (or modified versions
of such code, with unchanged license). You may copy and distribute such a system following the terms
of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.
 */
package com.example.howzit;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.lifecycle.MutableLiveData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PasswordStrengthEstimator implements TextWatcher {
    MutableLiveData<String> strengthLevel = new MutableLiveData<String>();
    MutableLiveData<Integer> strengthColor = new MutableLiveData<Integer>();
    MutableLiveData<Integer> lowerCase = new MutableLiveData();
    MutableLiveData<Integer> upperCase = new MutableLiveData();
    MutableLiveData<Integer> digit = new MutableLiveData();
    MutableLiveData<Integer> special = new MutableLiveData();


    // Define initial values
    public PasswordStrengthEstimator(){
        strengthLevel.setValue("Invalid");
        strengthColor.setValue(R.color.weak_password);
        lowerCase.setValue(0);
        upperCase.setValue(0);
        digit.setValue(0);
        special.setValue(0);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(charSequence != null) {
            // Check if the password contains a lower case letter
            lowerCase.setValue(hasLowerCase(charSequence));

            // Check if the password contains an upper case letter
            upperCase.setValue(hasUpperCase(charSequence));

            // Check if the password contains a digit
            digit.setValue(hasDigit(charSequence));

            // Check if the password contains a special character
            upperCase.setValue(hasSpecial(charSequence));

            // Estimate character strength
            estimate(charSequence);
        }
    }

    private void estimate(CharSequence charSequence) {
        int passwordLength = charSequence.toString().length();
        if(passwordLength < 6){
            strengthColor.setValue(R.color.invalid_password);
            strengthLevel.setValue("Invalid");
        } else if(passwordLength < 8){
            strengthColor.setValue(R.color.weak_password);
            strengthLevel.setValue("Weak");
        } else if (passwordLength >= 8 && passwordLength < 11) {
            if(lowerCase.getValue() + upperCase.getValue() + digit.getValue() + special.getValue() >= 2){
                strengthColor.setValue(R.color.medium_password);
                strengthLevel.setValue("Medium");
            }
        } else {
            if(lowerCase.getValue() + upperCase.getValue() + digit.getValue() + special.getValue() >= 3) {
                strengthColor.setValue(R.color.strong_password);
                strengthLevel.setValue("Strong");
            } else{
                strengthColor.setValue(R.color.medium_password);
                strengthLevel.setValue("Medium");
            }
        }
    }

    private int hasLowerCase(CharSequence charSequence) {
        Pattern pattern = Pattern.compile("[a-z]");
        Matcher matcher = pattern.matcher(charSequence);
        return (matcher.find()) ? 1 : 0;
    }

    private int hasUpperCase(CharSequence charSequence) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(charSequence);
        return (matcher.find()) ? 1 : 0;
    }

    private int hasDigit(CharSequence charSequence) {
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(charSequence);
        return (matcher.find()) ? 1 : 0;
    }

    private int hasSpecial(CharSequence charSequence) {
        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(charSequence);
        return (matcher.find()) ? 1 : 0;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void afterTextChanged(Editable editable) {}
}
