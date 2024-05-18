using System;
using System.Net;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;

namespace MESSENGER
{
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                Server server = new Server();
                server.Show();
                this.Hide(); 
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Ошибка при создании сервера: {ex.Message}");
            }
        }

        private void Button_Click_1(object sender, RoutedEventArgs e)
        {
            string ip = IpBox.Text;
            string name = NameBox.Text;

            if (IsValidIp(ip) && IsValidName(name))
            {
                try
                {
                    Messenger messenger = new Messenger(ip, name);
                    messenger.Show();
                    this.Hide(); 
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Ошибка при подключении к серверу: {ex.Message}");
                }
            }
            else
            {
                MessageBox.Show("Неверный IP или имя пользователя.");
            }
        }

        private bool IsValidIp(string ip)
        {
            string pattern = @"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
            return Regex.IsMatch(ip, pattern);
        }

        private bool IsValidName(string name)
        {
            
            if (string.IsNullOrWhiteSpace(name))
            {
                return false;
            }

            
            

            return true;
        }

        private void IpBox_TextChanged(object sender, TextChangedEventArgs e)
        {
            
        }

        private void NameBox_TextChanged(object sender, TextChangedEventArgs e)
        {
            
        }
    }
}