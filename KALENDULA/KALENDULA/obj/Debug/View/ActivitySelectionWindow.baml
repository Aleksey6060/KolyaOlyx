   M S B A M L   `   `   `  ����D  @KALENDULA, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null9!clr-namespace:KALENDULA.ViewModelKALENDULA.ViewModel  5   	   &     KALENDAR.ActivitySelectionWindow   5   	   R NWindowsBase, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35\ XPresentationFramework, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35W SPresentationCore, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35D 9http://schemas.microsoft.com/winfx/2006/xaml/presentation    5   	   R NSystem.Xaml, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5   	   0vm!clr-namespace:KALENDULA.ViewModel     5   	     � Title$'  !Выбор активностей��5   	   $	��440��6#   $	��270��60    � DataContext 5      4   .KALENDULA.ViewModel.ActivitySelectionViewModel  5   
   .��5	      �  � RowDefinitions 5
   
   �� 5      $
��Auto �6   �� 5      $��* �6   �� 5      $
��Auto �6   .+�5   
   �� $,��&Выберите активности:��6   $��16(�60   $��
10,10,10,0q�6>   $��0��6R   �� 5   
   -   5         ��Name$ stackPanelActivities��5       �� Orientation$ Vertical=�63   $��10q�6J   $��1��6V   5      �� 5   
   $ 
Horizontal=�6   $��Right=�6.   $��
10,0,10,10q�6J   $��2��6^   .+�5      �� -   5         ��Name$ btnSave��5      $��Сохранить��6&   $��5q�6Z   ��6:   �� *SaveCommand+6Z   �� 5      -   5        $ 	btnCancel��5      $��Отмена��6(   $��5q�6[   ��69   �� *CancelCommand+6[   5      5      5      