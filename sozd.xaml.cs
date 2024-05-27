using System;
using System.Data;
using System.Windows;
using Microsoft.Win32;
using Spire.Xls;

namespace Exel
{
    public partial class sozd : Window
    {
        public sozd()
        {
            InitializeComponent();
            InitializeEmptyDataTable();
        }

        private void InitializeEmptyDataTable()
        {
            DataTable dataTable = new DataTable();

            dataTable.Columns.Add("Колонка 1");
            

            
            Kreeed.ItemsSource = dataTable.DefaultView;
        }

        private void Zagr(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog
            {
                Filter = "Excel Files|*.xls;*.xlsx;*.xlsm",
                Title = "Выберите файл Excel"
            };

            if (openFileDialog.ShowDialog() == true)
            {
                try
                {
                    Workbook wb = new Workbook();
                    wb.LoadFromFile(openFileDialog.FileName);

                    Worksheet sheet = wb.Worksheets[0];
                    CellRange locateRange = sheet.AllocatedRange;

                    var dataTable = sheet.ExportDataTable(locateRange, true);
                    Kreeed.ItemsSource = dataTable.DefaultView;

                    MessageBox.Show("Файл успешно загружен.", "Успех", MessageBoxButton.OK, MessageBoxImage.Information);
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Ошибка при загрузке файла: {ex.Message}", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
        }

        private void Sohr(object sender, RoutedEventArgs e)
        {
            SaveFileDialog saveFileDialog = new SaveFileDialog
            {
                Filter = "Excel Files|*.xls;*.xlsx;*.xlsm",
                Title = "Сохранить файл Excel",
                FileName = "файл проги.xlsx"
            };

            if (saveFileDialog.ShowDialog() == true)
            {
                try
                {
                    var dataTable = Kreeed.ItemsSource as DataView;
                    if (dataTable == null)
                    {
                        MessageBox.Show("Нет данных для сохранения.", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }

                    Workbook wb = new Workbook();
                    wb.Worksheets.Clear();
                    Worksheet sheet = wb.Worksheets.Add("Лист 1");

                    sheet.InsertDataView(dataTable, true, 1, 1);
                    wb.SaveToFile(saveFileDialog.FileName, Spire.Xls.FileFormat.Version2016);

                    MessageBox.Show("Файл успешно сохранен.", "Успех", MessageBoxButton.OK, MessageBoxImage.Information);
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Ошибка при сохранении файла: {ex.Message}", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
        }

        private void NovST_GotFocus(object sender, RoutedEventArgs e)
        {
            if (NovST.Text == "Введите название колонки")
            {
                NovST.Text = "";
            }
        }

        private void NovST_LostFocus(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(NovST.Text))
            {
                NovST.Text = "Введите название колонки";
            }
        }

        private void Dob(object sender, RoutedEventArgs e)
        {
            string columnName = NovST.Text;

            if (!string.IsNullOrEmpty(columnName) && columnName != "Введите название колонки")
            {
                
                DataView dataView = Kreeed.ItemsSource as DataView;
                if (dataView != null)
                {
                    DataTable dataTable = dataView.Table;

                    
                    if (!dataTable.Columns.Contains(columnName))
                    {
                       
                        dataTable.Columns.Add(columnName);

                        
                        Kreeed.ItemsSource = null;
                        Kreeed.ItemsSource = dataTable.DefaultView;

                        
                        Kreeed.UpdateLayout();

                        
                        NovST.Text = "Введите название колонки";
                    }
                    else
                    {
                        MessageBox.Show("Столбец с таким именем уже существует.", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                else
                {
                    MessageBox.Show("Источник данных DataGrid не является DataView.", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
            else
            {
                MessageBox.Show("Имя столбца не может быть пустым.", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }
}
