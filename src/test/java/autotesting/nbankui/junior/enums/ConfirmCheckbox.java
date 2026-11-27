package autotesting.nbankui.junior.enums;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.checked;

public enum ConfirmCheckbox {
    CHECKED {
        @Override
        public void apply(SelenideElement checkbox) {
            checkbox.click();
            checkbox.shouldBe(checked);
        }
    },
    UNCHECKED {
        @Override
        public void apply(SelenideElement checkbox) {
            checkbox.shouldNotBe(checked); // Ничего не нажимаем, только проверяем
        }
    };

    public abstract void apply(SelenideElement checkbox);
}
