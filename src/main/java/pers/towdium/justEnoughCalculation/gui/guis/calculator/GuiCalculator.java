package pers.towdium.justEnoughCalculation.gui.guis.calculator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import pers.towdium.justEnoughCalculation.JustEnoughCalculation;
import pers.towdium.justEnoughCalculation.core.Calculator;
import pers.towdium.justEnoughCalculation.core.ItemStackWrapper;
import pers.towdium.justEnoughCalculation.gui.commom.GuiTooltipScreen;
import pers.towdium.justEnoughCalculation.gui.commom.recipe.ContainerRecipe;
import pers.towdium.justEnoughCalculation.gui.guis.recipeEditor.ContainerRecipeEditor;
import pers.towdium.justEnoughCalculation.gui.guis.recipeEditor.GuiRecipeEditor;
import pers.towdium.justEnoughCalculation.gui.guis.recipePicker.GuiRecipePicker;
import pers.towdium.justEnoughCalculation.network.packets.PacketSyncRecord;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Towdium
 */
public class GuiCalculator extends GuiTooltipScreen{
    GuiTextField textFieldAmount;
    GuiButton buttonLeft;
    GuiButton buttonRight;
    GuiButton buttonEdit;
    GuiButton buttonMode;
    GuiButton buttonCalculate;
    GuiButton buttonView;
    Calculator.CostRecord costRecord;
    int activeSlot = -1;
    int page = 1;
    int total = 1;
    EnumMode mode = EnumMode.INPUT;
    ItemStack buffer;

    public enum EnumMode {INPUT, OUTPUT, CATALYST}

    public GuiCalculator(ContainerCalculator containerCalculator){
        super(containerCalculator);
        JustEnoughCalculation.networkWrapper.sendToServer(new PacketSyncRecord());
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonLeft = new GuiButton(4, guiLeft+7, guiTop+139, 20, 20, "<");
        buttonRight = new GuiButton(5, guiLeft+65, guiTop+139, 20, 20, ">");
        buttonEdit = new GuiButton(3, guiLeft+89, guiTop+7, 38, 20, StatCollector.translateToLocal("gui.calculator.edit"));
        buttonCalculate = new GuiButton(1, guiLeft+7, guiTop+31, 78, 20, StatCollector.translateToLocal("gui.calculator.calculate"));
        buttonView = new GuiButton(7, guiLeft+131, guiTop+7, 38, 20, StatCollector.translateToLocal("gui.calculator.view"));
        buttonList.add(buttonCalculate);
        buttonList.add(new GuiButton(2, guiLeft+89, guiTop+31, 80, 20, StatCollector.translateToLocal("gui.calculator.add")));
        buttonList.add(buttonEdit);
        buttonList.add(buttonView);
        buttonList.add(buttonLeft);
        buttonList.add(buttonRight);
        buttonMode = new GuiButton(6, guiLeft+89, guiTop+139, 80, 20, StatCollector.translateToLocal("gui.calculator.input"));
        buttonList.add(buttonMode);
        textFieldAmount = new GuiTextField(0, fontRendererObj, guiLeft+39, guiTop+8, 45, 18);
        Slot dest = inventorySlots.getSlot(0);
        ItemStack itemStack = ((ContainerCalculator)inventorySlots).getPlayer().getHeldItem();
        dest.inventory.setInventorySlotContents(dest.getSlotIndex(), ItemStackWrapper.NBT.getItem(itemStack, "dest"));
        textFieldAmount.setText(ItemStackWrapper.NBT.getString(itemStack, "text"));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(JustEnoughCalculation.Reference.MODID,"textures/gui/guiCalculator.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if(activeSlot == 0){
            Slot slot = inventorySlots.getSlot(activeSlot);
            this.drawTexturedModalRect(this.guiLeft+slot.xDisplayPosition-2, this.guiTop+slot.yDisplayPosition-2, 176, 0, 20, 20);
        }else if(activeSlot > 0){
            Slot slot = inventorySlots.getSlot(activeSlot);
            this.drawTexturedModalRect(this.guiLeft+slot.xDisplayPosition-1, this.guiTop+slot.yDisplayPosition-1, 196, 0, 18, 18);
        }
        textFieldAmount.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        fontRendererObj.drawString("x", 30, 13, 4210752);
        drawCenteredString(fontRendererObj, page + "/" + total, 46, 145, 4210752);
    }

    @Override
    protected String GetButtonTooltip(int buttonId) {
        switch (buttonId){
            case 2: return StatCollector.translateToLocal("gui.calculator.addTooltip");
            case 3: return StatCollector.translateToLocal("gui.calculator.editTooltip");
            case 7: return StatCollector.translateToLocal("gui.calculator.viewTooltip");
        }

        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id){
            case 1:
                if(inventorySlots.getSlot(0).getStack() != null){
                    refreshRecipe();
                }
                break;
            case 2:
                JustEnoughCalculation.proxy.getPlayerHandler().syncItemCalculator(inventorySlots.getSlot(0).getStack(), textFieldAmount.getText());
                mc.displayGuiScreen(new GuiRecipeEditor(new ContainerRecipeEditor(), this));
                break;
            case 3:
                List<Integer> list;
                if (activeSlot == -1) {
                    list = JustEnoughCalculation.proxy.getPlayerHandler().getAllRecipeIndexOf(inventorySlots.getSlot(0).getStack(), null);
                }else {
                    list = JustEnoughCalculation.proxy.getPlayerHandler().getAllRecipeIndexOf(inventorySlots.getSlot(activeSlot).getStack(), null);
                }

                mc.displayGuiScreen(new GuiRecipePicker(new ContainerRecipe(), this, list));
                break;
            case 4:
                if(page>1){
                    page--;
                }
                break;
            case 5:
                if(page<total){
                    page++;
                }
                break;
            case 6:
                switch (mode){
                    case INPUT:
                        mode = EnumMode.OUTPUT;
                        break;
                    case OUTPUT:
                        mode = EnumMode.CATALYST;
                        break;
                    case CATALYST:
                        mode = EnumMode.INPUT;
                        break;
                }
                page = 1;
                break;
            case 7:
                mc.displayGuiScreen(new GuiRecipePicker(new ContainerRecipe(), this, JustEnoughCalculation.proxy.getPlayerHandler().getAllRecipeIndex(null)));
                break;
        }
        updateLayout();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        JustEnoughCalculation.proxy.getPlayerHandler().syncItemCalculator(inventorySlots.getSlot(0).getStack(), textFieldAmount.getText());
        textFieldAmount.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        Slot slot = getSlotUnderMouse();
        if(slot != null && slot.getSlotIndex() == 0 && mouseButton == 0){
            setActiveSlot(slot.getSlotIndex());
            ((ContainerCalculator)inventorySlots).getPlayer().playSound("random.click", 1f, 1f );
            updateLayout();
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.textFieldAmount.textboxKeyTyped(typedChar, keyCode)){
            if(keyCode == 1){
                if(activeSlot != -1){
                    inventorySlots.getSlot(activeSlot).putStack(buffer);
                    setActiveSlot(-1);
                }else {
                    ((ContainerCalculator)inventorySlots).getPlayer().closeScreen();
                }
            }
        }else {
            JustEnoughCalculation.proxy.getPlayerHandler().syncItemCalculator(inventorySlots.getSlot(0).getStack(), textFieldAmount.getText());
        }

    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        ModelManager modelManager = null;
        Field[] fields = mc.getClass().getDeclaredFields();
        for(Field field : fields){
            if(ModelManager.class.equals(field.getType())){
                field.setAccessible(true);
                try {
                    modelManager = (ModelManager) field.get(mc);
                    break;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(modelManager != null){
            itemRender = new RenderItem(mc.renderEngine, modelManager){
                @Override
                public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
                    boolean b = fr.getUnicodeFlag();
                    fr.setUnicodeFlag(true);
                    super.renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, ItemStackWrapper.getDisplayAmount(stack));
                    fr.setUnicodeFlag(b);
                }
            };
        }
        costRecord = null;
        updateLayout();
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        if (slotIn != null)
        {
            slotId = slotIn.slotNumber;
        }
        mc.thePlayer.openContainer.slotClick(slotId, clickedButton, clickType, mc.thePlayer);
    }

    public void updateLayout(){
        switch (mode){
            case OUTPUT:
                buttonMode.displayString = StatCollector.translateToLocal("gui.calculator.output");
                break;
            case INPUT:
                buttonMode.displayString = StatCollector.translateToLocal("gui.calculator.input");
                break;
            case CATALYST:
                buttonMode.displayString = StatCollector.translateToLocal("gui.calculator.catalyst");
                break;
        }
        boolean b = JustEnoughCalculation.proxy.getPlayerHandler().getHasRecipeOf(inventorySlots.getSlot(0).getStack(), null);
        if(activeSlot == -1){
            buttonEdit.enabled = b;
        }else {
            buttonEdit.enabled = JustEnoughCalculation.proxy.getPlayerHandler().getHasRecipeOf(inventorySlots.getSlot(activeSlot).getStack(), null);
        }
        buttonCalculate.enabled = b;
        buttonView.enabled = JustEnoughCalculation.proxy.getPlayerHandler().getHasRecipe(null);
        if(costRecord != null){
            switch (mode){
                case OUTPUT:
                    total = (costRecord.getOutputStack().length+35)/36;
                    fillSlotsWith(costRecord.getOutputStack(), (page-1)*36);break;
                case INPUT:
                    total = (costRecord.getInputStack().length+35)/36;
                    fillSlotsWith(costRecord.getInputStack(), (page-1)*36);break;
                case CATALYST:
                    total = (costRecord.getCatalystStack().length+35)/36;
                    fillSlotsWith(costRecord.getCatalystStack(), (page-1)*36);break;
            }
        }else {
            fillSlotsWith(new ItemStack[0], 0);
        }
        buttonLeft.enabled = page != 1;
        buttonRight.enabled = page < total;
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int activeSlot) {
        if(activeSlot == -1){
            JustEnoughCalculation.proxy.getPlayerHandler().syncItemCalculator(inventorySlots.getSlot(0).getStack(), textFieldAmount.getText());
        }else {
            buffer = inventorySlots.getSlot(activeSlot).getStack();
        }
        this.activeSlot = activeSlot;
        updateLayout();
    }

    public void drawCenteredStringWithoutShadow(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawString(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
    }

    private void fillSlotsWith(ItemStack[] itemStacks, int start){
        int pos = 1;
        for(int i=start; i<start+36; i++){
            if(i<=itemStacks.length-1){
                inventorySlots.getSlot(pos++).putStack(itemStacks[i]);
            }else {
                inventorySlots.putStackInSlot(pos++, null);
            }
        }
    }

    private void refreshRecipe(){
        try {
            int i = Integer.valueOf(textFieldAmount.getText());
            Calculator calculator = new Calculator(inventorySlots.getSlot(0).getStack(), i*100);
            Calculator.CostRecord record = calculator.getCost();
            record.unify();
            costRecord = record;
        } catch (Exception e){
            textFieldAmount.setTextColor(16711680);
            TimerTask r = new TimerTask() {
                @Override
                public void run() {
                    textFieldAmount.setTextColor(14737632);
                }
            };
            Timer t = new Timer();
            t.schedule(r, 1000);
        }
    }
}